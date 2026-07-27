package domain

case class Choice[S](condition: AgentContext[S] => Boolean, actions: AgentContext[S] => List[Action[S]])

case class Decision[S](choices: List[Choice[S]]):
  def decide(ctx: AgentContext[S]): List[Action[S]] = choices.find(_.condition(ctx)).map(_.actions(ctx)).getOrElse(Nil)
