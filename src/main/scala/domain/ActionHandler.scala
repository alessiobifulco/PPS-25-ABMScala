package domain

case class ActionContext[S](sender: Agent[S], space: Space, freshId: AgentId, tick: Int)

trait ActionHandler[S]:
  def apply(action: Action[S], recipient: Agent[S], ctx: ActionContext[S]): List[Agent[S]]

object ActionHandler:

  def default[S]: ActionHandler[S] = DefaultHandler[S]()

  private case class DefaultHandler[S]() extends ActionHandler[S]:

    override def apply(action: Action[S], recipient: Agent[S], ctx: ActionContext[S]): List[Agent[S]] = action match
      case Die()                 => List.empty
      case Spawn(state)          => List(recipient, Agent(ctx.freshId, ctx.space.randomPosition, V2d.random(), state))
      case Remember(event)       => List(recording(recipient, ctx.tick, event))
      case ShareMemory(_, event) => List(recording(recipient, ctx.tick, event))
      case _                     => List(recipient)

    private def recording(agent: Agent[S], tick: Int, event: MemoryEvent): Agent[S] = agent
      .withMemory(agent.memory.map(_.remember(tick, event)))
