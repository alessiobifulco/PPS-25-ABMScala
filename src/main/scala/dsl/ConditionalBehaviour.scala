package dsl

import domain.*

object ConditionalBehaviour:

  type ActionSource[S] = AgentContext[S] => List[Action[S]]

  def behaviour[S](block: SimulationBuilder[S] ?=> ActionSource[S])(using builder: SimulationBuilder[S]): Unit =
    val default = block(using builder)
    builder.addChoice(Choice((_: AgentContext[S]) => true, default))

  extension [S](source: ActionSource[S])
    infix def whenAgentIs(state: S)(using builder: SimulationBuilder[S]): Unit = builder
      .addChoice(Choice((ctx: AgentContext[S]) => ctx.focus.state == state, source))

  def moveRandomly[S](speed: Double): ActionSource[S] = ctx =>
    ctx.focus.velocity match
      case v if v.length > 0 && math.random() > 0.05 => List(Move(v.normalized * speed))
      case _                                         => List(Move(V2d.random() * speed))

  def moveHorizontally[S](speed: Double): ActionSource[S] = ctx =>
    ctx.focus.velocity.x match
      case vx if vx < 0 => List(Move(V2d(-speed, 0)))
      case _            => List(Move(V2d(speed, 0)))
