package engine

import domain.*

object SimulationEngine:

  private case class Intent[S](agent: Agent[S], actions: List[Action[S]])

  private case class Population[S](agents: List[Agent[S]], nextId: Int):

    def newId: AgentId = AgentId(nextId)

    def updating(target: AgentId)(successorsOf: Agent[S] => List[Agent[S]]): Population[S] =
      val updated = agents.flatMap:
        case agent if agent.id == target => successorsOf(agent)
        case agent                       => List(agent)
      Population(updated, nextAvailableId(updated).max(nextId))

  def init[S](config: SimulationConfig[S]): SimulationState[S] =
    SimulationState(config.initialEnvironment, 0, nextAvailableId(config.initialEnvironment.agents))

  def tick[S](state: SimulationState[S], config: SimulationConfig[S]): SimulationState[S] =
    val intents = perceive(state, config).map(decide(state.environment, config))
    val population = deliver(intents, state, config)
    SimulationState(state.environment.withAgents(population.agents), state.tick + 1, population.nextId)

  private def nextAvailableId[S](agents: List[Agent[S]]): Int = agents
    .foldLeft(0)((next, agent) => next.max(agent.id.value + 1))

  private def perceive[S](state: SimulationState[S], config: SimulationConfig[S]): List[AgentContext[S]] =
    val findNeighbors = state.environment.neighborhoods(config.perceptionRadius)(using config.neighborStrategy)
    state.environment.agents.map(agent => AgentContext(agent, findNeighbors(agent), state.tick))

  private def decide[S](environment: Environment[S], config: SimulationConfig[S])(ctx: AgentContext[S]): Intent[S] =
    val actions = config.behavior(ctx)
    val moved = move(ctx.focus, actions, environment)
    config.rule(ctx) match
      case Some(state) => Intent(moved.withState(state), actions)
      case _           => Intent(moved, actions)

  private def deliver[S](
      intents: List[Intent[S]],
      state: SimulationState[S],
      config: SimulationConfig[S]
  ): Population[S] =
    val decided = Population(intents.map(_.agent), state.nextId)
    intents.foldLeft(decided): (population, intent) =>
      intent.actions.foldLeft(population): (current, action) =>
        route(action, intent.agent, current, state.environment.space, state.tick)(using config.actionHandler)

  private def route[S](action: Action[S], sender: Agent[S], population: Population[S], space: Space, tick: Int)(using
      handler: ActionHandler[S]
  ): Population[S] =
    val ctx = ActionContext(sender, space, population.newId, tick)
    population.updating(action.recipient(sender.id))(handler(action, _, ctx))

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
