package engine

import domain.*
import domain.Action.*

object SimulationEngine:

  private case class Intent[S](agent: Agent[S], actions: List[Action[S]])

  private case class Population[S](agents: List[Agent[S]], nextId: Int):

    def newId: AgentId = AgentId(nextId)

    def joinedBy(survivors: List[Agent[S]], newborns: List[Agent[S]]): Population[S] =
      Population(agents ++ survivors ++ newborns, nextId + newborns.size)

  def init[S](config: SimulationConfig[S]): SimulationState[S] =
    SimulationState(config.initialEnvironment, 0, nextAvailableId(config.initialEnvironment.agents))

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
      population.joinedBy(survivors(intent, state.tick), newborns(intent, population.newId, state.environment.space))

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

  private def newborns[S](intent: Intent[S], firstId: AgentId, space: Space): List[Agent[S]] = intent.actions
    .collect { case Spawn(state) => state }.zipWithIndex
    .map((state, offset) => Agent(AgentId(firstId.value + offset), space.randomPosition, V2d.random(), state))

  private def messages[S](intents: List[Intent[S]]): List[(AgentId, MemoryEvent)] = intents.flatMap(_.actions)
    .collect { case Tell(target, event) => (target, event) }

  private def deliver[S](agents: List[Agent[S]], messages: List[(AgentId, MemoryEvent)], tick: Int): List[Agent[S]] =
    agents.map: agent =>
      messages.collect { case (target, event) if target == agent.id => event }
        .foldLeft(agent)((recipient, event) => recording(recipient, event, tick))

  private def residenciesOf[S](agents: List[Agent[S]], state: SimulationState[S]): Map[AgentId, Residency] = agents
    .map(agent => agent.id -> stayOf(agent, state.environment.pois, state.residencyOf(agent.id))).toMap

  private def stayOf[S](agent: Agent[S], pois: List[POI], previous: Residency): Residency = pois.foldLeft(previous)(
    (residency, poi) => if poi.contains(agent.position) then residency.tickFor(poi.id) else residency.reset(poi.id)
  )

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
