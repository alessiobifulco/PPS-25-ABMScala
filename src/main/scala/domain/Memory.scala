package domain

enum MemoryEvent:
  case Sighting(poi: PoiId, position: P2d)
  case Encounter(other: AgentId, positive: Boolean)

case class Belief(event: MemoryEvent, at: Int)

trait Memory:
  def beliefs: List[Belief]
  def remember(tick: Int, event: MemoryEvent): Memory
  def latest: Option[Belief]
  def sightings: List[Belief]

object Memory:

  def empty(capacity: Int): Memory =
    require(capacity > 0, "Memory capacity must be positive")
    MemoryImpl(List.empty, capacity)

  private case class MemoryImpl(beliefs: List[Belief], capacity: Int) extends Memory:

    override def remember(tick: Int, event: MemoryEvent): Memory = copy(beliefs =
      (beliefs :+ Belief(event, tick)).takeRight(capacity)
    )

    override def latest: Option[Belief] = beliefs.lastOption

    override def sightings: List[Belief] = beliefs.collect:
      case belief @ Belief(_: MemoryEvent.Sighting, _) => belief
