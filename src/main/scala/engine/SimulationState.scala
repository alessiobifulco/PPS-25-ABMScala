package engine

import domain.*

/** Represents an immutable snapshot of the entire simulation at a specific point in time. It encapsulates all the
  * dynamic properties of the system, evolving step-by-step as the [[SimulationEngine]] computes consecutive ticks.
  *
  * @param environment
  *   The current state of the simulated world, holding the active population, topology, and POIs.
  * @param tick
  *   The current discrete time step (or frame) of the simulation sequence.
  * @param nextId
  *   An internal counter used to safely generate strictly unique [[AgentId]]s for newly spawned agents.
  * @param residencies
  *   A mapping tracking, for each agent, how many consecutive ticks it has been standing inside each Point of Interest.
  * @tparam S
  *   The generic type representing the internal state of the Agents.
  */
case class SimulationState[S](
    environment: Environment[S],
    tick: Int,
    nextId: Int = 0,
    residencies: Map[AgentId, Residency] = Map.empty
):

  /** Retrieves the residency currently accumulated by a specific agent.
    *
    * @param id
    *   The unique identifier ([[AgentId]]) of the target agent.
    * @return
    *   The [[Residency]] data for the agent, or a pristine, empty residency if no prior presence was recorded.
    */
  def residencyOf(id: AgentId): Residency = residencies.getOrElse(id, Residency.empty)
