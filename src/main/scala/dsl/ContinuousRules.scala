package dsl

import domain.*

/** A type class representing the capability to treat a generic state as a continuous numeric variable. It acts as an
  * adapter, allowing the DSL to perform mathematical operations (like averaging or interpolating) on abstract domain
  * states.
  *
  * Being a type class, the adaptation happens outside the user's type: a domain state becomes continuous by the mere
  * existence of a given instance in scope, without inheriting anything from the library and without acquiring any
  * dependency towards it.
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
    * the average state of its surrounding neighbors. It is highly useful for modeling consensus, flocking alignment, or
    * heat diffusion. The rule applies whatever the current state of the agent is, and it fires only when at least one
    * influencing neighbor is perceived, so that the average is always computed on a non-empty set.
    *
    * The state type is constrained by the context bound `S: Continuous`, meaning the rule becomes applicable to a
    * domain state only if the corresponding [[Continuous]] adapter can be summoned in scope: a missing adapter is a
    * compilation error raised where the rule is declared, never a failure at run time.
    *
    * @param within
    *   The maximum spatial radius to consider neighbors as influential. Defaults to infinity, and it can only narrow
    *   the perception radius configured for the simulation.
    * @param among
    *   A predicate deciding whether the focus agent (second parameter) is influenced by the neighbor (first parameter),
    *   based on their respective states.
    * @param atRate
    *   The interpolation factor (speed of convergence) towards the average. 1.0 means instant alignment, lower values
    *   mean gradual shifts.
    * @param builder
    *   The implicit [[RulesBuilder]] where the resulting rule will be stored.
    * @tparam S
    *   The internal state of the Agent, required to be adaptable to a continuous variable.
    */
  def convergeTowardsAverage[S: Continuous](
      within: Double = Double.PositiveInfinity,
      among: (S, S) => Boolean = (_: S, _: S) => true,
      atRate: Double = 1.0
  )(using builder: RulesBuilder[S]): Unit =

    val continuous = summon[Continuous[S]]

    def influencing(ctx: AgentContext[S]): List[Agent[S]] = ctx.visibleWithin(within)
      .filter(neighbor => among(neighbor.state, ctx.focus.state))

    def averaged(ctx: AgentContext[S]): S =
      val own = continuous.extract(ctx.focus.state)
      val others = influencing(ctx).map(neighbor => continuous.extract(neighbor.state))
      continuous.update(ctx.focus.state, own + (others.sum / others.size - own) * atRate)

    builder.add(InteractionRule(Option.empty[S], influencing(_).nonEmpty)(averaged))
