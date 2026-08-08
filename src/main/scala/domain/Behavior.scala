package domain

trait Behavior[S]:
  def apply(ctx: AgentContext[S]): List[Action[S]]

object Behavior:

  def apply[S](f: AgentContext[S] => List[Action[S]]): Behavior[S] = f(_)

  def fromDecision[S](decision: Decision[S]): Behavior[S] = Behavior(decision.decide)

  extension [S](b: Behavior[S]) def andThen(other: Behavior[S]): Behavior[S] = Behavior(ctx => b(ctx) ++ other(ctx))
