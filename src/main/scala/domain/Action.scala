package domain

trait Action[S]:
  def recipient(sender: AgentId): AgentId = sender

case class Move[S](velocity: V2d) extends Action[S]

case class Spawn[S](state: S) extends Action[S]

case class Die[S]() extends Action[S]

case class Remember[S](event: MemoryEvent) extends Action[S]

case class Forget[S]() extends Action[S]

case class ShareMemory[S](target: AgentId, event: MemoryEvent) extends Action[S]:
  override def recipient(sender: AgentId): AgentId = target
