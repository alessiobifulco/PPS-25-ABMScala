package domain

opaque type AgentId = Int

object AgentId:
  def apply(value: Int): AgentId = value

  extension (id: AgentId) def value: Int = id
