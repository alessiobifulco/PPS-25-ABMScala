package dsl

import domain.*

import scala.annotation.tailrec

object BehaviorDsl:
  def moveRandomly[S](speed: Double): AgentContext[S] => List[Action[S]] = ctx =>
    ctx.focus.velocity match
      case v if v.length > 0 && math.random() > 0.05 => List(Move(v.normalized * speed))
      case _                                         => List(Move(V2d.random() * speed))

  def moveHorizontally[S](speed: Double): AgentContext[S] => List[Action[S]] = ctx =>
    ctx.focus.velocity.x match
      case vx if vx < 0 => List(Move(V2d(-speed, 0)))
      case _            => List(Move(V2d(speed, 0)))

  private def centroidOffset[S](from: P2d, group: List[Agent[S]]): V2d =
    if (group.nonEmpty)
      val centerX: Double = group.map(_.position.x).sum / group.size
      val centerY = group.map(_.position.y).sum / group.size
      P2d(centerX, centerY) - from
    else V2d.zero

  private def averageHeading[S](group: List[Agent[S]]): V2d =
    if group.isEmpty then V2d.zero
    else
      val avgX = group.map(_.velocity.x).sum / group.size
      val avgY = group.map(_.velocity.y).sum / group.size
      V2d(avgX, avgY)

  private def separationOffset[S](from: P2d, repellers: List[Agent[S]]): V2d =
    @tailrec
    def loop(remaining: List[Agent[S]], acc: V2d): V2d = remaining match
      case Nil          => acc
      case head :: tail =>
        val away = from - head.position
        val next = if away.length > 0 then acc + (away * (1.0 / away.length)) else acc
        loop(tail, next)
    loop(repellers, V2d.zero)

  def flock[S](
      speed: Double,
      separationRadius: Double,
      similarTo: (S, S) => Boolean,
      cohesionWeight: Double = 1.0,
      alignmentWeight: Double = 1.0,
      separationWeight: Double = 1.5,
      inertiaWeight: Double = 0.5
  ): AgentContext[S] => List[Action[S]] = ctx =>
    val (alike, different) = ctx.neighbors.partition(n => similarTo(n.state, ctx.focus.state))
    val tooClose = ctx.neighbors.filter(n => (n.position - ctx.focus.position).length < separationRadius)
    val repellers = different ++ tooClose
    val cohOffset = centroidOffset(ctx.focus.position, alike)
    val cohesion = if cohOffset.length > 0 then cohOffset.normalized else V2d.zero
    val heading = averageHeading(alike)
    val alignment = if heading.length > 0 then heading.normalized else V2d.zero
    val sepRaw = separationOffset(ctx.focus.position, repellers)
    val separation = if sepRaw.length > 0 then sepRaw.normalized else V2d.zero
    val inertia = if ctx.focus.velocity.length > 0 then ctx.focus.velocity.normalized else V2d.random()
    val direction = (cohesion * cohesionWeight) + (alignment * alignmentWeight) + (separation * separationWeight) +
      (inertia * inertiaWeight)
    val finalDirection = if direction.length > 0 then direction.normalized else inertia
    List(Move(finalDirection * speed))
