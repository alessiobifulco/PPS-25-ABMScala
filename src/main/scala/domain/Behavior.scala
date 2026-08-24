package domain

trait Behavior[S]:
  def whenState: Option[S]
  def actions(ctx: AgentContext[S]): List[Action[S]]
  def appliesTo(ctx: AgentContext[S]): Boolean = whenState.forall(_ == ctx.focus.state)

object Behavior:

  def apply[S](whenState: Option[S])(actions: AgentContext[S] => List[Action[S]]): Behavior[S] =
    BehaviorImpl(whenState, actions)

  private case class BehaviorImpl[S](whenState: Option[S], run: AgentContext[S] => List[Action[S]]) extends Behavior[S]:

    override def actions(ctx: AgentContext[S]): List[Action[S]] = run(ctx)
