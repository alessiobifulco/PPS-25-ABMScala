package domain

/** Represents a state transition rule for an [[Agent]]. It determines how an agent's internal state evolves in response
  * to its environment and social interactions, as captured by the [[AgentContext]]. By design, the state of an agent is
  * meant to evolve only through the interaction with other agents or with the Points of Interest.
  *
  * @tparam S
  *   The generic type representing the internal state of the Agent.
  */
trait InteractionRule[S]:

  /** The specific internal state required for this rule to be active. If empty, the rule applies regardless of the
    * agent's current state.
    */
  def whenState: Option[S]

  /** An additional situational [[Condition]] that must be satisfied for the rule to trigger.
    */
  def context: Condition[S]

  /** Evaluates the current worldview of the agent to compute its subsequent state.
    *
    * @param ctx
    *   The current [[AgentContext]] characterizing the agent's situation.
    * @return
    *   The newly evaluated state of type [[S]].
    */
  def newState(ctx: AgentContext[S]): S

  /** Determines whether this rule is applicable by verifying both the prerequisite state and the contextual condition
    * against the current situation.
    *
    * @param ctx
    *   The current [[AgentContext]] to check against.
    * @return
    *   True if the rule applies to the current context, false otherwise.
    */
  def appliesTo(ctx: AgentContext[S]): Boolean = whenState.forall(_ == ctx.focus.state) && context(ctx)

object InteractionRule:

  /** Creates a new [[InteractionRule]] instance.
    *
    * @param whenState
    *   The optional state prerequisite required to trigger the rule.
    * @param context
    *   The situational [[Condition]] that must be met.
    * @param newState
    *   A function mapping the current [[AgentContext]] to the updated state.
    * @return
    *   A defined [[InteractionRule]].
    */
  def apply[S](whenState: Option[S], context: Condition[S])(newState: AgentContext[S] => S): InteractionRule[S] =
    InteractionRuleImpl(whenState, context, newState)

  private case class InteractionRuleImpl[S](whenState: Option[S], context: Condition[S], run: AgentContext[S] => S)
      extends InteractionRule[S]:

    override def newState(ctx: AgentContext[S]): S = run(ctx)
