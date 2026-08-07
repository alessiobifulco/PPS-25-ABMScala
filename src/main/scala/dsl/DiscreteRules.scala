package dsl

import domain.*

trait Discrete[S]

object Discrete:
  given enumIsDiscrete[S <: scala.reflect.Enum]: Discrete[S] = new Discrete[S] {}

object DiscreteRules:

  case class NeighbourCount(matches: Int => Boolean)

  def atLeastNear(n: Int): NeighbourCount = NeighbourCount(_ >= n)
  def exactlyNear(n: Int): NeighbourCount = NeighbourCount(_ == n)
  def fewerNear(n: Int): NeighbourCount = NeighbourCount(_ < n)

  case class Chance(probability: Double):
    require(probability >= 0.0 && probability <= 1.0, "Probability must be between 0 and 1")

  def chance(probability: Double): Chance = Chance(probability)

  final class CountingConfig[S](result: S, count: Int => Boolean):
    private var neighbourState: Option[S] = None
    private var fromState: Option[S] = None

    infix def withState(state: S): CountingConfig[S] =
      neighbourState = Some(state)
      this

    infix def whenAgentIs(state: S)(using builder: SimulationBuilder[S]): Unit =
      fromState = Some(state)
      builder.addRule(build())

    private def build(): InteractionRule[S] = ctx =>
      Option.when(
        fromState.contains(ctx.focus.state) && count(ctx.neighbors.count(n => neighbourState.contains(n.state)))
      )(result)

  final class ChanceConfig[S](result: S, probability: Double):
    infix def whenAgentIs(state: S)(using builder: SimulationBuilder[S]): Unit = builder
      .addRule(ctx => Option.when(ctx.focus.state == state && math.random() < probability)(result))

  extension [S](result: S)(using Discrete[S])
    infix def when(count: NeighbourCount): CountingConfig[S] = CountingConfig(result, count.matches)

    infix def when(c: Chance): ChanceConfig[S] = ChanceConfig(result, c.probability)
