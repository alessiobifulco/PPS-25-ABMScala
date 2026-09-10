package engine

import domain.*
import domain.Action.*

/** The execution core of the simulation. It advances the system in discrete steps (ticks): at each tick every agent
  * perceives its surroundings, its behaviors and rules produce the actions it intends to perform and its new internal
  * state, and the engine resolves those actions (movement, births, deaths, memory updates and messages) into a new
  * [[SimulationState]].
  *
  * A tick never modifies the state it receives. It is not fully deterministic, though: newborns get a random velocity,
  * and behaviors and rules may draw random numbers themselves.
  */
object SimulationEngine:

  /** An internal wrapper pairing an agent (with its newly computed physical/internal state) with the list of actions it
    * intends to perform during the current tick.
    */
  private case class Intent[S](agent: Agent[S], actions: List[Action[S]])

  /** An internal accumulator used during the "grow" phase: it collects surviving agents and newborns together with the
    * next available identifier, so that adding agents and assigning identifiers happen in the same step.
    */
  private case class Population[S](agents: List[Agent[S]], nextId: Int):

    def newId: AgentId = AgentId(nextId)

    def joinedBy(survivors: List[Agent[S]], newborns: List[Agent[S]]): Population[S] =
      Population(agents ++ survivors ++ newborns, nextId + newborns.size)

  /** Creates the initial state at tick zero. The first identifier assigned to newborns follows the highest one already
    * in use, so that newborns never collide with the initial agents.
    *
    * @param config
    *   The [[SimulationConfig]] defining the starting setup.
    * @return
    *   The initial [[SimulationState]].
    */
  def init[S](config: SimulationConfig[S]): SimulationState[S] =
    SimulationState(config.initialEnvironment, 0, nextAvailableId(config.initialEnvironment.agents))

  /** Advances the simulation by one tick, through the following phases:
    *   - perceive: an [[AgentContext]] is built for every agent, with the neighbors found by the configured
    *     [[NeighborStrategy]] within the perception radius, the current tick and the agent's residency. The neighbor
    *     search is prepared once per tick and then queried for every agent.
    *   - decide: the first applicable [[Behavior]] produces the agent's [[Action]]s. The agent moves by the sum of the
    *     requested velocities, or keeps its current velocity when no movement is requested, and the [[BoundaryPolicy]]
    *     resolves the crossing of the borders. The first applicable [[InteractionRule]], evaluated on the same context,
    *     computes the new internal state.
    *   - grow: an agent asking to die leaves the population, an agent asking to remember updates its own memory, and
    *     every spawn request adds a newborn with a random velocity and a fresh [[AgentId]], placed where its parent has
    *     moved during this tick. Death removes only the agent itself: the newborns and the messages it requested in the
    *     same tick are still produced. The resulting population keeps the order of the previous one, each surviving
    *     agent followed by the agents it has spawned.
    *   - deliver: every message is recorded in the memory of the agent it is addressed to; a message addressed to an
    *     agent that is no longer in the population, or that has no memory, is dropped.
    *   - residenciesOf: the residency counters are incremented for the Points of Interest containing the agent and
    *     reset for the others.
    *
    * @param state
    *   The current [[SimulationState]].
    * @param config
    *   The static [[SimulationConfig]] providing the rules and behaviors.
    * @return
    *   The [[SimulationState]] of the next tick.
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
    val actions = actionsFor(ctx, config.behaviors)
    Intent(evolved(move(ctx.focus, actions, environment), ctx, config.rules), actions)

  private def actionsFor[S](ctx: AgentContext[S], behaviors: List[Behavior[S]]): List[Action[S]] = behaviors
    .find(_.appliesTo(ctx)).map(_.actions(ctx)).getOrElse(List.empty)

  private def evolved[S](agent: Agent[S], ctx: AgentContext[S], rules: List[InteractionRule[S]]): Agent[S] = rules
    .find(_.appliesTo(ctx)).map(_.newState(ctx)).fold(agent)(agent.withState)

  private def grow[S](intents: List[Intent[S]], state: SimulationState[S]): Population[S] = intents
    .foldLeft(Population(List.empty[Agent[S]], state.nextId)): (population, intent) =>
      population.joinedBy(survivors(intent, state.tick), newborns(intent, population.newId))

  private def survivors[S](intent: Intent[S], tick: Int): List[Agent[S]] =
    if intent.actions.exists(isDeath) then List.empty
    else List(recording(intent.agent, remembered(intent.actions), tick))

  private def remembered[S](actions: List[Action[S]]): List[MemoryEvent] = actions.collect:
    case Remember(event) => event

  private def recording[S](agent: Agent[S], events: List[MemoryEvent], tick: Int): Agent[S] = agent
    .withMemory(agent.memory.map(memory => events.foldLeft(memory)(_.remember(tick, _))))

  private def isDeath[S](action: Action[S]): Boolean = action match
    case Die() => true
    case _     => false

  private def newborns[S](intent: Intent[S], firstId: AgentId): List[Agent[S]] = intent.actions
    .collect { case Spawn(state) => state }.zipWithIndex
    .map((state, offset) => Agent(AgentId(firstId.value + offset), intent.agent.position, V2d.random(), state))

  private def messages[S](intents: List[Intent[S]]): List[(AgentId, MemoryEvent)] = intents.flatMap(_.actions)
    .collect { case Tell(target, event) => (target, event) }

  private def deliver[S](agents: List[Agent[S]], messages: List[(AgentId, MemoryEvent)], tick: Int): List[Agent[S]] =
    agents.map(agent => recording(agent, inboxOf(agent.id, messages), tick))

  private def inboxOf(id: AgentId, messages: List[(AgentId, MemoryEvent)]): List[MemoryEvent] = messages
    .filter(_._1 == id).map(_._2)

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
    case velocities => velocities.reduce(_ + _)

  private def moves[S](actions: List[Action[S]]): List[V2d] = actions.collect:
    case Move(velocity) => velocity
