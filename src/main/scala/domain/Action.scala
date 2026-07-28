package domain

trait Action[S]

case class Move[S](velocity: V2d) extends Action[S]

case class ShareMemory[S](targetId: AgentId, event: S) extends Action[S]
