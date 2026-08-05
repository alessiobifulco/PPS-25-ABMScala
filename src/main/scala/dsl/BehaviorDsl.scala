package dsl

import domain.*

object BehaviorDsl:
  def moveRandomly[S](speed: Double): AgentContext[S] => List[Action[S]] = ctx =>
    ctx.focus.velocity match
      case v if v.length > 0 && math.random() > 0.05 => List(Move(v.normalized * speed))
      case _                                         => List(Move(V2d.random() * speed))

  def moveHorizontally[S](speed: Double): AgentContext[S] => List[Action[S]] = ctx =>
    ctx.focus.velocity.x match
      case vx if vx < 0 => List(Move(V2d(-speed, 0)))
      case _            => List(Move(V2d(speed, 0)))
