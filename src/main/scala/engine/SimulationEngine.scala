package engine

import domain.*

object SimulationEngine:

  def init[S](config: SimulationConfig[S]): SimulationState[S] = SimulationState(config.initialEnvironment, 0)

  def tick[S](state: SimulationState[S], config: SimulationConfig[S]): SimulationState[S] =
    val environment = state.environment
    val agents = perceive(state, config).map(step(environment, config))
    SimulationState(environment.withAgents(agents), state.tick + 1)

  private def step[S](environment: Environment[S], config: SimulationConfig[S])(ctx: AgentContext[S]): Agent[S] =
    val moved = move(environment, config.behavior)(ctx)
    config.rule(ctx).fold(moved)(moved.withState)

  private def perceive[S](state: SimulationState[S], config: SimulationConfig[S]): List[AgentContext[S]] =
    val findNeighbors = state.environment.neighborhoods(config.perceptionRadius)(using config.neighborStrategy)
    state.environment.agents.map(agent => AgentContext(agent, findNeighbors(agent), state.tick))

  private def move[S](environment: Environment[S], behavior: Behavior[S])(ctx: AgentContext[S]): Agent[S] =
    val agent = ctx.focus
    val velocity = velocityOf(behavior(ctx), agent.velocity)
    val (position, resolved) = environment.boundaryPolicy(agent.position + velocity, velocity, environment.space)
    agent.withMotion(position, resolved)

  private def velocityOf[S](actions: List[Action[S]], current: V2d): V2d = moves(actions).reduceOption(_ + _)
    .getOrElse(current)

  private def moves[S](actions: List[Action[S]]): List[V2d] = actions.flatMap:
    case Move(v) => List(v)
    case _       => Nil
