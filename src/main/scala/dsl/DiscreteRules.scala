package dsl

import domain.*

object DiscreteRules:

  private type Condition[S] = AgentContext[S] => Boolean

  def atLeastNear[S](n: Int, state: S): Condition[S] = ctx => ctx.neighbors.count(_.state == state) >= n

  def exactlyNear[S](n: Int, state: S): Condition[S] = ctx => ctx.neighbors.count(_.state == state) == n

  def fewerNear[S](n: Int, state: S): Condition[S] = ctx => ctx.neighbors.count(_.state == state) < n

  def chanceOf[S](probability: Double): Condition[S] = _ => Chance(probability).happens

  def inside[S](poi: POI): Condition[S] = ctx => ctx.isInside(poi)

  def settledIn[S](poi: POI): Condition[S] = ctx => ctx.hasSettledIn(poi)

  extension [S](condition: Condition[S])

    infix def and(other: Condition[S]): Condition[S] = ctx => condition(ctx) && other(ctx)

    infix def or(other: Condition[S]): Condition[S] = ctx => condition(ctx) || other(ctx)

  final class RuleConfig[S](result: S, condition: Condition[S]) extends RuleBuilder[S]:

    private var fromState: Option[S] = None

    infix def whenAgentIs(state: S): RuleConfig[S] =
      fromState = Some(state)
      this

    override def build(): InteractionRule[S] = fromState match
      case Some(from) => ctx =>
          ctx.focus.state match
            case s if s == from && condition(ctx) => Some(result)
            case _                                => None

      case None => throw new IllegalStateException(
          "Cannot build the rule: initial state is missing. Please append 'whenAgentIs(state)' to your rule."
        )

  extension [S](result: S)
    infix def when(condition: Condition[S])(using r: RulesBuilder[S]): RuleConfig[S] =
      val config = RuleConfig(result, condition)
      r.addRuleBuilder(config)
      config
