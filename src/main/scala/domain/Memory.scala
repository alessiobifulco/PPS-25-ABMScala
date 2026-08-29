package domain

/** Represents an event that an agent can record. It forms the foundational unit of knowledge within an agent's
  * [[Memory]].
  */
enum MemoryEvent:

  /** The discovery or observation of a Point of Interest.
    *
    * @param poi
    *   The unique identifier ([[PoiId]]) of the observed Point of Interest.
    * @param position
    *   The spatial location ([[P2d]]) where it was sighted.
    */
  case Sighting(poi: PoiId, position: P2d)

  /** A social interaction or meeting with another agent.
    *
    * @param other
    *   The unique identifier ([[AgentId]]) of the encountered agent.
    * @param positive
    *   A flag indicating the nature of the encounter (e.g., friendly vs hostile).
    */
  case Encounter(other: AgentId, positive: Boolean)

/** A time-stamped piece of knowledge held by an agent. It anchors a specific [[MemoryEvent]] to the exact moment (tick)
  * it was experienced or recorded.
  *
  * @param event
  *   The recorded experience.
  * @param at
  *   The simulation step (tick) when the memory was acquired.
  */
case class Belief(event: MemoryEvent, at: Int)

/** Represents the cognitive storage capacity of an [[Agent]]. It manages the accumulation of [[Belief]]s over time
  * using an immutable, bounded architecture.
  */
trait Memory:

  /** The full collection of beliefs currently held in memory.
    */
  def beliefs: List[Belief]

  /** Creates an updated copy of the memory containing the newly recorded event. If the memory has reached its maximum
    * capacity, the oldest belief is discarded (FIFO strategy).
    *
    * @param tick
    *   The current simulation step.
    * @param event
    *   The [[MemoryEvent]] to record.
    * @return
    *   A new [[Memory]] instance reflecting the accumulated knowledge.
    */
  def remember(tick: Int, event: MemoryEvent): Memory

  /** Retrieves the most recently acquired belief, if the memory is not empty.
    */
  def latest: Option[Belief]

  /** Filters the memory to extract only the beliefs related to spatial observations.
    *
    * @return
    *   A list of [[Belief]]s wrapping a [[MemoryEvent.Sighting]].
    */
  def sightings: List[Belief]

object Memory:

  /** Creates a new, empty [[Memory]] with a strictly defined retention limit.
    *
    * @param capacity
    *   The maximum number of beliefs the agent can hold before forgetting older ones.
    * @return
    *   A fresh [[Memory]] instance.
    * @throws IllegalArgumentException
    *   if the given capacity is not strictly positive.
    */
  def apply(capacity: Int): Memory =
    require(capacity > 0, "Memory capacity must be positive")
    MemoryImpl(List.empty, capacity)

  private case class MemoryImpl(beliefs: List[Belief], capacity: Int) extends Memory:

    override def remember(tick: Int, event: MemoryEvent): Memory = copy(beliefs =
      (beliefs :+ Belief(event, tick)).takeRight(capacity)
    )

    override def latest: Option[Belief] = beliefs.lastOption

    override def sightings: List[Belief] = beliefs.collect:
      case belief @ Belief(_: MemoryEvent.Sighting, _) => belief
