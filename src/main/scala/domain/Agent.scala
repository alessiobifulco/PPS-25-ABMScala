package domain

trait Agent[S]:
  def id: AgentId
  def position: P2d
  def velocity: V2d
  def state: S
  def memory: Option[Memory[S]]

object Agent:
  def apply[S](id: AgentId, position: P2d, velocity: V2d, state: S, memory: Option[Memory[S]] = None): Agent[S] =
    AgentImpl(id, position, velocity, state, memory)

  private case class AgentImpl[S](id: AgentId, position: P2d, velocity: V2d, state: S, memory: Option[Memory[S]])
      extends Agent[S]
