package dsl

import domain.*

object RulesDSL:

  def whenNear[S](fromState: S, neighborState: S, minNeighbors: Int, toState: S): InteractionRule[S] = ctx =>
    Option.when(ctx.focus.state == fromState && ctx.neighbors.count(_.state == neighborState) >= minNeighbors)(toState)

  def byChance[S](fromState: S, probability: Double, toState: S): InteractionRule[S] =
    ctx => Option.when(ctx.focus.state == fromState && math.random() < probability)(toState)
