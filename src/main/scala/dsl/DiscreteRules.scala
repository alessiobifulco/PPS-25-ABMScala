package dsl

import domain.*

object DiscreteRules:

  trait Transition[S]:
    def result: S
    def from: S

  private object Transition:

    def apply[S](result: S, from: S): Transition[S] = TransitionImpl(result, from)

    private case class TransitionImpl[S](result: S, from: S) extends Transition[S]

  def atLeastNear[S](n: Int, state: S): Condition[S] = ctx => ctx.neighbors.count(_.state == state) >= n

  def exactlyNear[S](n: Int, state: S): Condition[S] = ctx => ctx.neighbors.count(_.state == state) == n

  def fewerNear[S](n: Int, state: S): Condition[S] = ctx => ctx.neighbors.count(_.state == state) < n

  def chanceOf[S](probability: Double): Condition[S] = _ => Chance(probability).happens

  def inside[S](poi: POI): Condition[S] = ctx => ctx.isInside(poi)

  def settledIn[S](poi: POI): Condition[S] = ctx => ctx.hasSettledIn(poi)

  extension [S](condition: Condition[S])

    infix def and(other: Condition[S]): Condition[S] = ctx => condition(ctx) && other(ctx)

    infix def or(other: Condition[S]): Condition[S] = ctx => condition(ctx) || other(ctx)

  extension [S](result: S) infix def whenAgentIs(from: S): Transition[S] = Transition(result, from)

  extension [S](transition: Transition[S])
    infix def iff(condition: Condition[S])(using builder: RulesBuilder[S]): Unit = builder
      .add(InteractionRule(Some(transition.from), condition)(_ => transition.result))
