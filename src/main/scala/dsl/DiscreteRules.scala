package dsl

import domain.*

/** Provides a suite of declarative conditions and syntax extensions designed to build discrete state transition rules
  * (e.g., finite state machines, cellular automata logic) within the DSL.
  */
object DiscreteRules:

  /** An intermediate DSL construct representing a pending state change. It links a target resulting state to a specific
    * starting state, awaiting a [[Condition]] to finalize the rule definition.
    *
    * @tparam S
    *   The generic type representing the internal state of the Agent.
    */
  trait Transition[S]:

    /** The state the agent will hold once the transition is triggered.
      */
    def result: S

    /** The state the agent must currently hold for the transition to be considered.
      */
    def from: S

  private object Transition:

    def apply[S](result: S, from: S): Transition[S] = TransitionImpl(result, from)

    private case class TransitionImpl[S](result: S, from: S) extends Transition[S]

  /** A condition satisfied when the agent perceives at least `n` neighbors holding the specified state.
    */
  def atLeastNear[S](n: Int, state: S): Condition[S] = ctx => ctx.neighbors.count(_.state == state) >= n

  /** A condition satisfied when the agent perceives exactly `n` neighbors holding the specified state.
    */
  def exactlyNear[S](n: Int, state: S): Condition[S] = ctx => ctx.neighbors.count(_.state == state) == n

  /** A condition satisfied when the agent perceives fewer than `n` neighbors holding the specified state.
    */
  def fewerNear[S](n: Int, state: S): Condition[S] = ctx => ctx.neighbors.count(_.state == state) < n

  /** A condition stochastically satisfied based on the given probability, re-evaluated at every tick.
    *
    * @throws IllegalArgumentException
    *   if the given probability lies outside the [0, 1] range.
    */
  def chanceOf[S](probability: Double): Condition[S] = _ => Chance(probability).happens

  /** A condition satisfied when the agent's current position is physically inside the given POI.
    */
  def inside[S](poi: POI): Condition[S] = ctx => ctx.isInside(poi)

  /** A condition satisfied when the agent has remained inside the given POI long enough to trigger its activation.
    */
  def settledIn[S](poi: POI): Condition[S] = ctx => ctx.hasSettledIn(poi)

  /** A condition satisfied when the agent lies at least the specified distance away from a target coordinate.
    */
  def farFrom[S](target: P2d, distance: Double): Condition[S] = ctx => (ctx.focus.position - target).length >= distance

  /** A condition satisfied if the agent still remembers sighting a POI inside the given time window.
    *
    * @param within
    *   The maximum number of ticks that can have elapsed since the sighting.
    */
  def recentlySighted[S](within: Int): Condition[S] =
    ctx => ctx.focus.remembers.exists(belief => isSighting(belief.event) && belief.at >= ctx.tick - within)

  /** A condition satisfied when no sighting of a POI is recorded inside the given time window, either because the agent
    * has never seen one, or because the memory it holds is already older than the window itself.
    *
    * @param ticks
    *   The duration (in ticks) defining the observation window.
    */
  def nothingSightedIn[S](ticks: Int): Condition[S] =
    ctx => ctx.focus.remembers.forall(belief => !isSighting(belief.event) || belief.at < ctx.tick - ticks)

  private def isSighting(event: MemoryEvent): Boolean = event match
    case MemoryEvent.Sighting(_, _) => true
    case _                          => false

  /** Boolean algebra combinators allowing the chaining of multiple [[Condition]]s fluently.
    */
  extension [S](condition: Condition[S])

    /** Combines two conditions using a logical AND.
      */
    infix def and(other: Condition[S]): Condition[S] = ctx => condition(ctx) && other(ctx)

    /** Combines two conditions using a logical OR.
      */
    infix def or(other: Condition[S]): Condition[S] = ctx => condition(ctx) || other(ctx)

  /** The first step of the discrete rule DSL syntax. It binds a target outcome state to a required starting state.
    */
  extension [S](result: S) infix def whenAgentIs(from: S): Transition[S] = Transition(result, from)

  /** The terminal step of the discrete rule DSL syntax. It attaches a trigger condition to a [[Transition]] and
    * automatically registers the finalized [[InteractionRule]] into the implicit [[RulesBuilder]].
    */
  extension [S](transition: Transition[S])
    infix def iff(condition: Condition[S])(using builder: RulesBuilder[S]): Unit = builder
      .add(InteractionRule(Some(transition.from), condition)(_ => transition.result))
