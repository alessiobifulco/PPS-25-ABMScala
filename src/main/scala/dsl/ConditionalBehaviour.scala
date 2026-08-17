package dsl

import domain.*
import dsl.DiscreteRules.Chance

object ConditionalBehaviour:

  type ActionSource[S] = AgentContext[S] => List[Action[S]]

  private val directionChangeChance = 0.05

  extension [S](source: ActionSource[S])

    infix def whenAgentIs(state: S)(using builder: ChoicesBuilder[S]): Unit = builder
      .addChoice(Choice(ctx => ctx.focus.state == state, source))

    infix def and(other: ActionSource[S]): ActionSource[S] = ctx => source(ctx) ++ other(ctx)

    infix def vanishingWith(c: Chance): ActionSource[S] = ctx =>
      math.random() match
        case p if p < c.probability => source(ctx) :+ Die()
        case _                      => source(ctx)

  def asDefault[S](source: ActionSource[S])(using builder: ChoicesBuilder[S]): Unit = builder.setDefault(source)

  def moveRandomly[S](speed: Double): ActionSource[S] = ctx =>
    ctx.focus.velocity match
      case v if v.length > 0 && math.random() > directionChangeChance => List(Move(v.normalized * speed))
      case _                                                          => List(Move(V2d.random() * speed))

  def moveHorizontally[S](speed: Double): ActionSource[S] = ctx =>
    ctx.focus.velocity.x match
      case vx if vx < 0 => List(Move(V2d(-speed, 0)))
      case _            => List(Move(V2d(speed, 0)))

  def stopMoving[S]: ActionSource[S] = _ => List(Move(V2d.zero))

  def die[S]: ActionSource[S] = _ => List(Die())

  def spawn[S](state: S): ActionSource[S] = _ => List(Spawn(state))
