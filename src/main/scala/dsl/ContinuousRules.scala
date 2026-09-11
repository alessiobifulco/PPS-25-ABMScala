package dsl

import domain.*

/** A type class representing the capability to treat a generic state as a continuous numeric variable. It acts as an
  * adapter, allowing the DSL to perform mathematical operations (like averaging or interpolating) on abstract domain
  * states.
  *
  * @tparam S
  *   The generic type representing the internal state of the Agent.
  */
trait Continuous[S]:

  /** Extracts the underlying numeric representation from the agent's state.
    */
  def extract(state: S): Double

  /** Reconstructs a new state instance incorporating the updated numeric value.
    */
  def update(state: S, value: Double): S

/** Provides a suite of pre-packaged [[InteractionRule]]s designed for environments where the agent's state models a
  * continuous spectrum (e.g., temperature, opinion, energy level).
  */
object ContinuousRules:

  /** A DSL helper that registers a synchronization rule: the agent adjusts its continuous state by blending it towards
    * the average state of its surrounding neighbors.
    *
    * The rule applies whatever the current state of the agent is, but only when at least one influencing neighbor
    * exists, so that the average is never computed on an empty set.
    *
    * @param within
    *   The maximum spatial radius to consider neighbors as influential. Defaults to infinity, and it can only narrow
    *   the perception radius configured for the simulation.
    * @param among
    *   A predicate deciding whether the focus agent (second parameter) is influenced by the neighbor (first parameter),
    *   based on their respective states.
    * @param atRate
    *   The interpolation factor towards the average, a proportion between 0.0 and 1.0: 1.0 means instant alignment,
    *   lower values mean gradual shifts.
    * @param builder
    *   The implicit [[RulesBuilder]] where the resulting rule will be stored.
    * @tparam S
    *   The internal state of the Agent, required to be adaptable to a continuous variable.
    * @throws IllegalArgumentException
    *   if the convergence rate lies outside the unit interval.
    */
  def convergeTowardsAverage[S: Continuous](
      within: Double = Double.PositiveInfinity,
      among: (S, S) => Boolean = (_: S, _: S) => true,
      atRate: Double = 1.0
  )(using builder: RulesBuilder[S]): Unit =
    require(atRate >= 0.0 && atRate <= 1.0, "Convergence rate must be between 0 and 1")

    val continuous = summon[Continuous[S]]

    def influencing(ctx: AgentContext[S]): List[Agent[S]] = ctx.visibleWithin(within)
      .filter(neighbor => among(neighbor.state, ctx.focus.state))

    def averaged(ctx: AgentContext[S]): S =
      val own = continuous.extract(ctx.focus.state)
      val others = influencing(ctx).map(neighbor => continuous.extract(neighbor.state))
      continuous.update(ctx.focus.state, own + (others.sum / others.size - own) * atRate)

    builder.add(InteractionRule(Option.empty[S], influencing(_).nonEmpty)(averaged))
