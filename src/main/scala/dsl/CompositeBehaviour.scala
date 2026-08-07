package dsl

import domain.*
import dsl.ConditionalBehaviour.ActionSource

object CompositeBehaviour:

  private def normalizedOrElse(v: V2d, fallback: => V2d): V2d = if v.length > 0 then v.normalized else fallback

  private def alike[S](neighbors: List[Agent[S]], focus: Agent[S], similarTo: (S, S) => Boolean): List[Agent[S]] =
    neighbors.filter(n => similarTo(n.state, focus.state))

  private def repellersOf[S](
      neighbors: List[Agent[S]],
      focus: Agent[S],
      separationRadius: Double,
      similarTo: (S, S) => Boolean
  ): List[Agent[S]] = neighbors
    .filter(n => !similarTo(n.state, focus.state) || (n.position - focus.position).length < separationRadius)

  private def inertiaForce[S](focus: Agent[S]): V2d = normalizedOrElse(focus.velocity, V2d.random())

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

  def flock[S](
      speed: Double,
      separationRadius: Double,
      similarTo: (S, S) => Boolean,
      cohesionWeight: Double = 1.0,
      alignmentWeight: Double = 1.0,
      separationWeight: Double = 1.5,
      inertiaWeight: Double = 0.5
  ): ActionSource[S] = ctx =>
    val neighborsAlike = alike(ctx.neighbors, ctx.focus, similarTo)
    val direction = (cohesionForce(ctx.focus, neighborsAlike) * cohesionWeight) +
      (alignmentForce(neighborsAlike) * alignmentWeight) +
      (separationForce(ctx.focus, repellersOf(ctx.neighbors, ctx.focus, separationRadius, similarTo)) *
        separationWeight) + (inertiaForce(ctx.focus) * inertiaWeight)
    List(Move(normalizedOrElse(direction, inertiaForce(ctx.focus)) * speed))
