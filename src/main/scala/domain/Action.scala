package domain

enum Action[S]:
  case Move(velocity: V2d) extends Action[S]
  case Remember(event: MemoryEvent) extends Action[S]
  case Tell(target: AgentId, event: MemoryEvent) extends Action[S]
  case Spawn(state: S) extends Action[S]
  case Die() extends Action[S]