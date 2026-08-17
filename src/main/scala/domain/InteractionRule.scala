package domain

trait InteractionRule[S]:
  def apply(ctx: AgentContext[S]): Option[S]

object InteractionRule:

  def apply[S](f: AgentContext[S] => Option[S]): InteractionRule[S] = f(_)

  def firstOf[S](rules: InteractionRule[S]*): InteractionRule[S] =
    ctx => rules.foldLeft(Option.empty[S])((result, rule) => result.orElse(rule(ctx)))
