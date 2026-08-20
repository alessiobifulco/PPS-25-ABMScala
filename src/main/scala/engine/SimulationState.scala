package engine

import domain.*

case class SimulationState[S](
    environment: Environment[S],
    tick: Int,
    nextId: Int = 0,
    residencies: Map[AgentId, Residency] = Map.empty
):
  def residencyOf(id: AgentId): Residency = residencies.getOrElse(id, Residency.empty)
