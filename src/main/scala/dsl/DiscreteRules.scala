package dsl

import domain.*

object DiscreteRules:

  case class NeighbourCount(matches: Int => Boolean)

  def atLeastNear(n: Int): NeighbourCount = NeighbourCount(_ >= n)
  def exactlyNear(n: Int): NeighbourCount = NeighbourCount(_ == n)
  def fewerNear(n: Int): NeighbourCount = NeighbourCount(_ < n)

  final class CountingConfig[S](result: S, count: Int => Boolean) extends RuleBuilder[S]:

    private var neighbourState: Option[S] = Option.empty
    private var fromState: Option[S] = Option.empty

    infix def withState(state: S): CountingConfig[S] =
      neighbourState = Some(state)
      this

    infix def whenAgentIs(state: S): CountingConfig[S] =
      fromState = Some(state)
      this

    override def build(): InteractionRule[S] =
      assert(
        (fromState, neighbourState).toList.forall(_.nonEmpty),
        "Cannot build without setting all parameters first!"
      )
      val from = fromState.get
      val neighbour = neighbourState.get
      ctx =>
        ctx.focus.state match
          case s if s == from && count(ctx.neighbors.count(_.state == neighbour)) => Some(result)
          case _                                                                  => Option.empty

  final class ChanceConfig[S](result: S, chance: Chance) extends RuleBuilder[S]:

    private var fromState: Option[S] = Option.empty

    infix def whenAgentIs(state: S): ChanceConfig[S] =
      fromState = Some(state)
      this

    override def build(): InteractionRule[S] =
      assert(fromState.nonEmpty, "Cannot build without setting all parameters first!")
      val from = fromState.get
      ctx =>
        ctx.focus.state match
          case s if s == from && chance.happens => Some(result)
          case _                                => Option.empty

  extension [S](result: S)

    infix def when(count: NeighbourCount)(using r: RulesBuilder[S]): CountingConfig[S] =
      val config = CountingConfig(result, count.matches)
      r.addRuleBuilder(config)
      config

    infix def when(c: Chance)(using r: RulesBuilder[S]): ChanceConfig[S] =
      val config = ChanceConfig(result, c)
      r.addRuleBuilder(config)
      config
