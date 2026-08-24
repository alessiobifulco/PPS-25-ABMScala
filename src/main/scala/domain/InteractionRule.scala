package domain

trait InteractionRule[S]:
  def whenState: Option[S]
  def context: Condition[S]
  def newState(ctx: AgentContext[S]): S
  def appliesTo(ctx: AgentContext[S]): Boolean = whenState.forall(_ == ctx.focus.state) && context(ctx)

object InteractionRule:

  def apply[S](whenState: Option[S], context: Condition[S])(newState: AgentContext[S] => S): InteractionRule[S] =
    InteractionRuleImpl(whenState, context, newState)

  private case class InteractionRuleImpl[S](whenState: Option[S], context: Condition[S], run: AgentContext[S] => S)
      extends InteractionRule[S]:

    override def newState(ctx: AgentContext[S]): S = run(ctx)
