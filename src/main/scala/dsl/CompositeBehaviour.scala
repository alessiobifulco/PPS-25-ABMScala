package dsl

import domain.*
import dsl.ConditionalBehaviour.ActionSource

object CompositeBehaviour:

  private def normalizedOrElse(v: V2d, fallback: => V2d): V2d = if v.length > 0 then v.normalized else fallback

  private def headingForce[S](focus: Agent[S]): V2d = normalizedOrElse(focus.velocity, V2d.random())

  private def cohesionForce[S](focus: Agent[S], alike: List[Agent[S]]): V2d =
    if alike.isEmpty then V2d.zero
    else
      normalizedOrElse(
        P2d(alike.map(_.position.x).sum / alike.size, alike.map(_.position.y).sum / alike.size) - focus.position,
        V2d.zero
      )

  private def alignmentForce[S](alike: List[Agent[S]]): V2d =
    if alike.isEmpty then V2d.zero
    else
      normalizedOrElse(
        V2d(alike.map(_.velocity.x).sum / alike.size, alike.map(_.velocity.y).sum / alike.size),
        V2d.zero
      )

  private def separationForce[S](focus: Agent[S], repellers: List[Agent[S]]): V2d = normalizedOrElse(
    repellers.foldLeft(V2d.zero)((acc, other) => acc + (focus.position - other.position).normalized),
    V2d.zero
  )

  final class FlockConfig[S](isFollowed: (S, S) => Boolean) extends ActionSource[S]:

    private var isAvoided: (S, S) => Boolean = (_, _) => false
    private var speed: Double = 1.0
    private var separationRadius: Double = 0.0
    private var cohesionWeight: Double = 1.0
    private var alignmentWeight: Double = 1.0
    private var separationWeight: Double = 1.5
    private var headingWeight: Double = 0.5

    infix def avoid(p: (S, S) => Boolean): FlockConfig[S] =
      isAvoided = p
      this

    infix def movingAt(s: Double): FlockConfig[S] =
      speed = s
      this

    infix def keepingApart(radius: Double): FlockConfig[S] =
      separationRadius = radius
      this

    infix def withCohesion(weight: Double): FlockConfig[S] =
      cohesionWeight = weight
      this

    infix def withAlignment(weight: Double): FlockConfig[S] =
      alignmentWeight = weight
      this

    infix def withSeparation(weight: Double): FlockConfig[S] =
      separationWeight = weight
      this

    infix def withHeading(weight: Double): FlockConfig[S] =
      headingWeight = weight
      this

    override def apply(ctx: AgentContext[S]): List[Action[S]] =
      val followed = ctx.neighbors.filter(n => isFollowed(n.state, ctx.focus.state))
      val avoided = ctx.neighbors
        .filter(n => isAvoided(n.state, ctx.focus.state) || (n.position - ctx.focus.position).length < separationRadius)
      val heading = headingForce(ctx.focus)
      val direction = (cohesionForce(ctx.focus, followed) * cohesionWeight) +
        (alignmentForce(followed) * alignmentWeight) + (separationForce(ctx.focus, avoided) * separationWeight) +
        (heading * headingWeight)
      List(Move(normalizedOrElse(direction, heading) * speed))

  def follow[S](p: (S, S) => Boolean): FlockConfig[S] = FlockConfig[S](p)
