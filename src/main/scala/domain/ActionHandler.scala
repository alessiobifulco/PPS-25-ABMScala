package domain

case class ActionContext[S](focus: Agent[S], space: Space, freshId: AgentId)

trait ActionHandler[S]:
  def apply(action: Action[S], agents: List[Agent[S]], ctx: ActionContext[S]): List[Agent[S]]

object ActionHandler:

  def default[S]: ActionHandler[S] = DefaultHandler[S]()

  private case class DefaultHandler[S]() extends ActionHandler[S]:

    override def apply(action: Action[S], agents: List[Agent[S]], ctx: ActionContext[S]): List[Agent[S]] = action match
      case Die()        => agents.filter(_.id != ctx.focus.id)
      case Spawn(state) => agents :+ Agent(ctx.freshId, ctx.space.randomPosition, V2d.random(), state)
      case _            => agents
