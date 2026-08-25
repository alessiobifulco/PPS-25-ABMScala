package dsl

import domain.*
import domain.Action.*
import dsl.ConditionalBehavior.ActionSource

/** Provides advanced, multi-force movement behaviors for agents. It includes a highly customizable flocking algorithm
  * based on the classic Boids model, blending cohesion, alignment, separation, and heading forces.
  */
object CompositeBehavior:

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

  /** A fluent DSL builder for configuring a complex flocking behavior. It allows tuning the physical properties (speed,
    * radius) and the relative weights of the fundamental steering forces that guide the agent's movement. Being itself
    * an [[ActionSource]], a configured instance can be plugged directly into the behavior DSL.
    *
    * @param isFollowed
    *   The baseline predicate defining which neighboring agents belong to the same flock.
    */
  final class FlockConfig[S](isFollowed: (S, S) => Boolean) extends ActionSource[S]:

    private var isAvoided: (S, S) => Boolean = (_, _) => false
    private var speed: Double = 1.0
    private var separationRadius: Double = 0.0
    private var cohesionWeight: Double = 1.0
    private var alignmentWeight: Double = 1.0
    private var separationWeight: Double = 1.5
    private var headingWeight: Double = 0.5

    /** Defines a condition marking neighboring agents that must be actively repelled: they feed the separation force
      * together with the ones falling inside the separation radius, without being excluded from cohesion and alignment.
      *
      * @param p
      *   A predicate evaluating if the focus agent (second parameter) should avoid the neighbor (first parameter).
      * @return
      *   The same builder instance, for fluent chaining.
      */
    infix def avoid(p: (S, S) => Boolean): FlockConfig[S] =
      isAvoided = p
      this

    /** Sets the absolute base movement speed of the flocking agent, defaulting to 1.0.
      *
      * @return
      *   The same builder instance, for fluent chaining.
      */
    infix def movingAt(s: Double): FlockConfig[S] =
      speed = s
      this

    /** Sets the physical radius within which the separation force activates, preventing agents from crowding too
      * closely together. Defaults to 0.0, meaning that only the explicitly avoided neighbors are repelled.
      *
      * @return
      *   The same builder instance, for fluent chaining.
      */
    infix def keepingApart(radius: Double): FlockConfig[S] =
      separationRadius = radius
      this

    /** Adjusts the influence of the cohesion force (the desire to steer toward the center of mass of the flock),
      * defaulting to 1.0.
      *
      * @return
      *   The same builder instance, for fluent chaining.
      */
    infix def withCohesion(weight: Double): FlockConfig[S] =
      cohesionWeight = weight
      this

    /** Adjusts the influence of the alignment force (the desire to steer in the same average direction as the flock),
      * defaulting to 1.0.
      *
      * @return
      *   The same builder instance, for fluent chaining.
      */
    infix def withAlignment(weight: Double): FlockConfig[S] =
      alignmentWeight = weight
      this

    /** Adjusts the influence of the separation force (the desire to steer away from crowded neighbors), defaulting to
      * 1.5.
      *
      * @return
      *   The same builder instance, for fluent chaining.
      */
    infix def withSeparation(weight: Double): FlockConfig[S] =
      separationWeight = weight
      this

    /** Adjusts the influence of the heading force (the desire to maintain the current forward momentum), defaulting to
      * 0.5.
      *
      * @return
      *   The same builder instance, for fluent chaining.
      */
    infix def withHeading(weight: Double): FlockConfig[S] =
      headingWeight = weight
      this

    /** Evaluates the configured flocking parameters against the current worldview of the agent, blending four steering
      * forces: cohesion, pointing to the center of mass of the followed neighbors; alignment, matching their average
      * direction; separation, pushing away from the avoided ones; and heading, preserving the current course, replaced
      * by a random one whenever the agent is still. The weighted sum is then normalized and scaled by the configured
      * speed.
      *
      * @param ctx
      *   The current [[AgentContext]].
      * @return
      *   A list containing a single [[Move]] action holding the resulting steering vector.
      */
    override def apply(ctx: AgentContext[S]): List[Action[S]] =
      val followed = ctx.neighbors.filter(n => isFollowed(n.state, ctx.focus.state))
      val avoided = ctx.neighbors
        .filter(n => isAvoided(n.state, ctx.focus.state) || (n.position - ctx.focus.position).length < separationRadius)
      val heading = headingForce(ctx.focus)
      val direction = (cohesionForce(ctx.focus, followed) * cohesionWeight) +
        (alignmentForce(followed) * alignmentWeight) + (separationForce(ctx.focus, avoided) * separationWeight) +
        (heading * headingWeight)
      List(Move[S](normalizedOrElse(direction, heading) * speed))

  /** The primary DSL entry point to instantiate a flocking behavior.
    *
    * @param p
    *   A predicate evaluating if the focus agent (second parameter) considers the neighbor (first parameter) part of
    *   its flock.
    * @return
    *   A highly customizable [[FlockConfig]] builder.
    */
  def follow[S](p: (S, S) => Boolean): FlockConfig[S] = FlockConfig[S](p)
