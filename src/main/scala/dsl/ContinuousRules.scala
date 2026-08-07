package dsl

import domain.*

trait Continuous[S]:
  def extract(state: S): Double
  def rebuild(value: Double): S

object Continuous:
  def instance[S](extractor: S => Double, builder: Double => S): Continuous[S] = new Continuous[S]:
    def extract(state: S): Double = extractor(state)
    def rebuild(value: Double): S = builder(value)

object ContinuousRules:

  final class ConvergeConfig[S](using continuous: Continuous[S]):
    private var radius: Double = Double.PositiveInfinity
    private var matches: (S, S) => Boolean = (_, _) => true
    private var rate: Double = 1.0

    infix def within(r: Double): ConvergeConfig[S] =
      radius = r
      this

    infix def among(p: (S, S) => Boolean): ConvergeConfig[S] =
      matches = p
      this

    infix def atRate(r: Double)(using builder: SimulationBuilder[S]): Unit =
      rate = r
      builder.rule(build())

    private def build(): InteractionRule[S] = ctx =>
      val influencing = ctx.visibleWithin(radius).filter(n => matches(n.state, ctx.focus.state))
      if influencing.isEmpty then None
      else
        val own = continuous.extract(ctx.focus.state)
        val target = influencing.map(n => continuous.extract(n.state)).sum / influencing.size
        Some(continuous.rebuild(own + (target - own) * rate))

  def convergeTowardsAverage[S](using continuous: Continuous[S]): ConvergeConfig[S] = ConvergeConfig[S]
