package domain

trait ActionGraph[S, Ctx]:
  def resolve(ctx: Ctx): List[Action[S]]

object ActionGraph:
  case class Leaf[S, Ctx](f: Ctx => List[Action[S]]) extends ActionGraph[S, Ctx]:
    def resolve(ctx: Ctx): List[Action[S]] = f(ctx)
  case class Branch[S, Ctx](condition: Ctx => Boolean, ifTrue: ActionGraph[S, Ctx], ifFalse: ActionGraph[S, Ctx])
      extends ActionGraph[S, Ctx]:
    def resolve(ctx: Ctx): List[Action[S]] = if condition(ctx) then ifTrue.resolve(ctx) else ifFalse.resolve(ctx)

  def leaf[S, Ctx](action: Action[S]): ActionGraph[S, Ctx] = Leaf(_ => List(action))
