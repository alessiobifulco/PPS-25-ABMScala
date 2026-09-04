package engine

import domain.*
import domain.Action.*

/** The core execution orchestrator of the simulation. It drives the system forward in discrete time steps (ticks) using
  * a purely functional, immutable approach. At each tick, it processes a full "perceive-decide-act" cycle: agents
  * perceive their surroundings, decide on a set of intents based on their behaviors and rules, and the engine resolves
  * these intents (movement, state transitions, births, deaths, memory updates, and messaging) into a brand-new
  * simulation state.
  */
object SimulationEngine:

  /** An internal wrapper pairing an agent (with its newly computed physical/internal state) with the list of actions it
    * intends to perform during the current tick.
    */
  private case class Intent[S](agent: Agent[S], actions: List[Action[S]])

  /** An internal accumulator used during the "grow" phase to safely aggregate surviving agents and newborns, while
    * keeping track of the next available unique identifier.
    */
  private case class Population[S](agents: List[Agent[S]], nextId: Int):

    def newId: AgentId = AgentId(nextId)

    def joinedBy(survivors: List[Agent[S]], newborns: List[Agent[S]]): Population[S] =
      Population(agents ++ survivors ++ newborns, nextId + newborns.size)

  /** Bootstraps the simulation, generating the pristine initial state at tick zero. It calculates the starting point
    * for ID generation based on the agents pre-existing in the environment.
    *
    * @param config
    *   The [[SimulationConfig]] defining the starting setup.
    * @return
    *   The initial [[SimulationState]].
    */
  def init[S](config: SimulationConfig[S]): SimulationState[S] =
    SimulationState(config.initialEnvironment, 0, nextAvailableId(config.initialEnvironment.agents))

  /** Advances the simulation by exactly one discrete step, orchestrating the whole pipeline:
    *   - perceive: a fresh [[AgentContext]] is built for every agent, gathering its neighbors through the configured
    *     [[NeighborStrategy]] within the perception radius, together with the current tick and residency.
    *   - decide: the first applicable [[Behavior]] produces the intended [[Action]]s, the agent is displaced by summing
    *     the requested velocities (keeping the current one when no movement is intended) and letting the
    *     [[BoundaryPolicy]] resolve the collision with the borders of the space, and the first applicable
    *     [[InteractionRule]] computes its new internal state.
    *   - grow: personal actions and lifecycles are applied, so that an agent asking to die leaves the population, an
    *     agent asking to remember updates its own memory, and every spawn request adds a newborn placed on the parent's
    *     position with a random velocity and a freshly generated [[AgentId]].
    *   - deliver: the messages addressed to a specific agent are routed to it and recorded in its memory.
    *   - residenciesOf: the permanence counters are incremented for the Points of Interest containing the agent and
    *     reset for the ones it has left.
    *
    * @param state
    *   The current [[SimulationState]].
    * @param config
    *   The static [[SimulationConfig]] providing the rules and behaviors.
    * @return
    *   A new, immutable [[SimulationState]] representing the next timeframe of the world.
    */
  def tick[S](state: SimulationState[S], config: SimulationConfig[S]): SimulationState[S] =
    val intents = perceive(state, config).map(decide(state.environment, config))
    val population = grow(intents, state)
    val agents = deliver(population.agents, messages(intents), state.tick)
    SimulationState(
      state.environment.withAgents(agents),
      state.tick + 1,
      population.nextId,
      residenciesOf(agents, state)
    )

  private def nextAvailableId[S](agents: List[Agent[S]]): Int = agents
    .foldLeft(0)((next, agent) => next.max(agent.id.value + 1))

  private def perceive[S](state: SimulationState[S], config: SimulationConfig[S]): List[AgentContext[S]] =
    val findNeighbors = state.environment.neighborhoods(config.perceptionRadius)(using config.neighborStrategy)
    state.environment.agents
      .map(agent => AgentContext(agent, findNeighbors(agent), state.tick, state.residencyOf(agent.id)))

  private def decide[S](environment: Environment[S], config: SimulationConfig[S])(ctx: AgentContext[S]): Intent[S] =
    val actions = config.behaviors.find(_.appliesTo(ctx)).map(_.actions(ctx)).getOrElse(List.empty)
    val moved = move(ctx.focus, actions, environment)
    Intent(config.rules.find(_.appliesTo(ctx)).map(_.newState(ctx)).fold(moved)(moved.withState), actions)

  private def grow[S](intents: List[Intent[S]], state: SimulationState[S]): Population[S] = intents
    .foldLeft(Population(List.empty[Agent[S]], state.nextId)): (population, intent) =>
      population.joinedBy(survivors(intent, state.tick), newborns(intent, population.newId))

  private def survivors[S](intent: Intent[S], tick: Int): List[Agent[S]] =
    if intent.actions.exists(isDeath) then List.empty
    else List(intent.actions.foldLeft(intent.agent)((agent, action) => applying(agent, action, tick)))

  private def applying[S](agent: Agent[S], action: Action[S], tick: Int): Agent[S] = action match
    case Remember(event) => recording(agent, event, tick)
    case _               => agent

  private def recording[S](agent: Agent[S], event: MemoryEvent, tick: Int): Agent[S] = agent
    .withMemory(agent.memory.map(_.remember(tick, event)))

  private def isDeath[S](action: Action[S]): Boolean = action match
    case Die() => true
    case _     => false

  private def newborns[S](intent: Intent[S], firstId: AgentId): List[Agent[S]] = intent.actions
    .collect { case Spawn(state) => state }.zipWithIndex
    .map((state, offset) => Agent(AgentId(firstId.value + offset), intent.agent.position, V2d.random(), state))

  private def messages[S](intents: List[Intent[S]]): List[(AgentId, MemoryEvent)] = intents.flatMap(_.actions)
    .collect { case Tell(target, event) => (target, event) }

  private def deliver[S](agents: List[Agent[S]], messages: List[(AgentId, MemoryEvent)], tick: Int): List[Agent[S]] =
    agents.map: agent =>
      messages.collect { case (target, event) if target == agent.id => event }
        .foldLeft(agent)((recipient, event) => recording(recipient, event, tick))

  private def residenciesOf[S](agents: List[Agent[S]], state: SimulationState[S]): Map[AgentId, Residency] = agents
    .map(agent => agent.id -> stayOf(agent, state.environment.pois, state.residencyOf(agent.id))).toMap

  private def stayOf[S](agent: Agent[S], poiList: List[POI], previous: Residency): Residency = poiList.foldLeft(
    previous
  )((residency, poi) => if poi.contains(agent.position) then residency.tickFor(poi.id) else residency.reset(poi.id))

  private def move[S](agent: Agent[S], actions: List[Action[S]], environment: Environment[S]): Agent[S] =
    val velocity = velocityOf(actions, agent.velocity)
    val (position, resolved) = environment.boundaryPolicy(agent.position + velocity, velocity, environment.space)
    agent.withMotion(position, resolved)

  private def velocityOf[S](actions: List[Action[S]], current: V2d): V2d = moves(actions) match
    case Nil        => current
    case velocities => velocities.foldLeft(V2d.zero)(_ + _)

  private def moves[S](actions: List[Action[S]]): List[V2d] = actions.flatMap:
    case Move(v) => List(v)
    case _       => List.empty
