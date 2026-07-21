package domain

trait Behavior[S, Ctx]:
  def apply(ctx: Ctx): List[Action[S]]

object Behavior:
  def apply[S, Ctx](f: Ctx => List[Action[S]]): Behavior[S, Ctx] = f(_)

  def fromGraph[S, Ctx](graph: ActionGraph[S, Ctx]): Behavior[S, Ctx] = ctx => graph.resolve(ctx)

  extension [S, Ctx](b: Behavior[S, Ctx])
    def andThen(other: Behavior[S, Ctx]): Behavior[S, Ctx] = ctx => b(ctx) ++ other(ctx)
