package domain

/** Represents the decision-making logic of an [[Agent]]. A behavior evaluates the agent's current [[AgentContext]] to
  * formulate a series of intended [[Action]]s.
  *
  * @tparam S
  *   The generic type representing the internal state of the Agent.
  */
trait Behavior[S]:

  /** The specific internal state required for this behavior to be active. If empty, the behavior is considered
    * state-independent and applies universally.
    */
  def whenState: Option[S]

  /** Evaluates the agent's current worldview to determine its intentions.
    *
    * @param ctx
    *   The current [[AgentContext]] characterizing the agent's local context.
    * @return
    *   A list of [[Action]]s the agent intends to perform.
    */
  def actions(ctx: AgentContext[S]): List[Action[S]]

  /** Determines whether this behavior is relevant based on the focus agent's current state.
    *
    * @param ctx
    *   The current [[AgentContext]] to check against.
    * @return
    *   True if the behavior applies to the current state, false otherwise.
    */
  def appliesTo(ctx: AgentContext[S]): Boolean = whenState.forall(_ == ctx.focus.state)

object Behavior:

  /** Creates a new [[Behavior]] instance.
    *
    * @param whenState
    *   The optional state condition required to trigger the behavior.
    * @param actions
    *   A function mapping the current [[AgentContext]] to a list of intended [[Action]]s.
    * @return
    *   A defined [[Behavior]].
    */
  def apply[S](whenState: Option[S])(actions: AgentContext[S] => List[Action[S]]): Behavior[S] =
    BehaviorImpl(whenState, actions)

  private case class BehaviorImpl[S](whenState: Option[S], run: AgentContext[S] => List[Action[S]]) extends Behavior[S]:

    override def actions(ctx: AgentContext[S]): List[Action[S]] = run(ctx)
