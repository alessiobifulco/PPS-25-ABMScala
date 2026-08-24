package domain

opaque type PoiId = Int

object PoiId:

  def apply(value: Int): PoiId = value

  extension (id: PoiId) def value: Int = id

case class POI(id: PoiId, name: String, position: P2d, radius: Double, activationDelay: Int = 0):
  require(radius > 0, "Radius must be positive")
  require(activationDelay >= 0, "Activation delay cannot be negative")

  def contains(p: P2d): Boolean = (p - position).length <= radius

case class Residency(perPoi: Map[PoiId, Int]):

  def tickFor(poi: PoiId): Residency = copy(perPoi = perPoi.updatedWith(poi)(t => Some(t.getOrElse(0) + 1)))

  def reset(poi: PoiId): Residency = copy(perPoi = perPoi - poi)

  def ticksIn(poi: PoiId): Int = perPoi.getOrElse(poi, 0)

object Residency:
  val empty: Residency = Residency(Map.empty)
