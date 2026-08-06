package dsl

import domain.*

object RulesDSL:

  def whenNear[S](fromState: S, neighborState: S, minNeighbors: Int, toState: S): InteractionRule[S] = ctx =>
    Option.when(ctx.focus.state == fromState && ctx.neighbors.count(_.state == neighborState) >= minNeighbors)(toState)

  def byChance[S](fromState: S, probability: Double, toState: S): InteractionRule[S] =
    ctx => Option.when(ctx.focus.state == fromState && math.random() < probability)(toState)

  def convergeTowardsAverage[S](
      radius: Double,
      matches: (S, S) => Boolean,
      rate: Double,
      extract: S => Double,
      build: Double => S
  ): InteractionRule[S] = ctx =>
    val influencing = ctx.visibleWithin(radius).filter(n => matches(n.state, ctx.focus.state))
    if influencing.isEmpty then None
    else
      val own = extract(ctx.focus.state)
      val average = influencing.map(n => extract(n.state)).sum / influencing.size
      Some(build(own + (average - own) * rate))
