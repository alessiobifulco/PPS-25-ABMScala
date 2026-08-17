package engine

import domain.*

object SimulationEngine:

  def init[S](config: SimulationConfig[S]): SimulationState[S] =
    val agents = config.initialEnvironment.agents
    SimulationState(config.initialEnvironment, 0, agents.foldLeft(0)((next, agent) => next.max(agent.id.value + 1)))

  def tick[S](state: SimulationState[S], config: SimulationConfig[S]): SimulationState[S] =
    val environment = state.environment
    val (agents, nextId) = perceive(state, config).foldLeft((List.empty[Agent[S]], state.nextId)):
      case ((accumulated, freshId), ctx) =>
        val (successors, updatedId) = step(environment, config, freshId)(ctx)
        (accumulated ++ successors, updatedId)
    SimulationState(environment.withAgents(agents), state.tick + 1, nextId)

  private def step[S](environment: Environment[S], config: SimulationConfig[S], freshId: Int)(
      ctx: AgentContext[S]
  ): (List[Agent[S]], Int) =
    val actions = config.behavior(ctx)
    val moved = move(environment, actions)(ctx)
    val updated = config.rule(ctx) match
      case Some(state) => moved.withState(state)
      case _           => moved
    interpret(actions, updated, environment.space, freshId)(using config.actionHandler)

  private def interpret[S](actions: List[Action[S]], agent: Agent[S], space: Space, freshId: Int)(using
      handler: ActionHandler[S]
  ): (List[Agent[S]], Int) = actions.foldLeft((List(agent), freshId)):
    case ((agents, id), action) =>
      val successors = handler(action, agents, ActionContext(agent, space, AgentId(id)))
      (successors, id + successors.count(_.id.value >= id))

  private def perceive[S](state: SimulationState[S], config: SimulationConfig[S]): List[AgentContext[S]] =
    val findNeighbors = state.environment.neighborhoods(config.perceptionRadius)(using config.neighborStrategy)
    state.environment.agents.map(agent => AgentContext(agent, findNeighbors(agent), state.tick))

  private def move[S](environment: Environment[S], actions: List[Action[S]])(ctx: AgentContext[S]): Agent[S] =
    val agent = ctx.focus
    val velocity = velocityOf(actions, agent.velocity)
    val (position, resolved) = environment.boundaryPolicy(agent.position + velocity, velocity, environment.space)
    agent.withMotion(position, resolved)

  private def velocityOf[S](actions: List[Action[S]], current: V2d): V2d = moves(actions) match
    case Nil        => current
    case velocities => velocities.foldLeft(V2d.zero)(_ + _)

  private def moves[S](actions: List[Action[S]]): List[V2d] = actions.flatMap:
    case Move(v) => List(v)
    case _       => List.empty
