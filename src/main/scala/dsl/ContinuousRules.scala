package dsl

import domain.*

trait Continuous[S]:
  def extract(state: S): Double
  def update(state: S, value: Double): S

object ContinuousRules:

  def convergeTowardsAverage[S](
      within: Double = Double.PositiveInfinity,
      among: (S, S) => Boolean = (_: S, _: S) => true,
      atRate: Double = 1.0
  )(using continuous: Continuous[S], builder: RulesBuilder[S]): Unit =

    def influencing(ctx: AgentContext[S]): List[Agent[S]] = ctx.visibleWithin(within)
      .filter(neighbor => among(neighbor.state, ctx.focus.state))

    def averaged(ctx: AgentContext[S]): S =
      val own = continuous.extract(ctx.focus.state)
      val others = influencing(ctx).map(neighbor => continuous.extract(neighbor.state))
      continuous.update(ctx.focus.state, own + (others.sum / others.size - own) * atRate)

    builder.add(InteractionRule(Option.empty[S], influencing(_).nonEmpty)(averaged))
