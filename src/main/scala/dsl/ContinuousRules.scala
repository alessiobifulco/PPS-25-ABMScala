package dsl

import domain.*

trait Continuous[S]:
  def extract(state: S): Double
  def update(state: S, value: Double): S

object ContinuousRules:

  final class ConvergeConfig[S](continuous: Continuous[S]) extends RuleBuilder[S]:

    private var radius: Double = Double.PositiveInfinity
    private var matches: (S, S) => Boolean = (_, _) => true
    private var rate: Double = 1.0

    infix def within(r: Double): ConvergeConfig[S] =
      radius = r
      this

    infix def among(p: (S, S) => Boolean): ConvergeConfig[S] =
      matches = p
      this

    infix def atRate(r: Double): ConvergeConfig[S] =
      rate = r
      this

    override def build(): InteractionRule[S] = ctx =>
      val influencing = ctx.visibleWithin(radius).filter(n => matches(n.state, ctx.focus.state))
      if influencing.isEmpty then Option.empty
      else
        val own = continuous.extract(ctx.focus.state)
        val target = influencing.map(n => continuous.extract(n.state)).sum / influencing.size
        Some(continuous.update(ctx.focus.state, own + (target - own) * rate))

  def convergeTowardsAverage[S: Continuous](using r: RulesBuilder[S]): ConvergeConfig[S] =
    val config = ConvergeConfig[S](summon[Continuous[S]])
    r.addRuleBuilder(config)
    config
