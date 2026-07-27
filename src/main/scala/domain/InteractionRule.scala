package domain

trait InteractionRule[S]:
  def apply(ctx: AgentContext[S]): Option[S]

object InteractionRule:
  def apply[S](f: AgentContext[S] => Option[S]): InteractionRule[S] = f(_)

  def firstOf[S](rules: InteractionRule[S]*): InteractionRule[S] = ctx =>
    rules.iterator.map(_.apply(ctx)).collectFirst:
      case Some(s) => s
