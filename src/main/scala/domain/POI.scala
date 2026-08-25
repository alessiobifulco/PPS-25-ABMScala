package domain

/** Opaque identifier used to uniquely identify a [[POI]].
  */
opaque type PoiId = Int

/** Factory and extension methods for [[PoiId]].
  */
object PoiId:

  /** Creates a point-of-interest identifier from an integer value.
    *
    * @param value
    *   integer value associated with the identifier.
    * @return
    *   a new point-of-interest identifier.
    */
  def apply(value: Int): PoiId = value

  /** Returns the integer value associated with the identifier.
    *
    * @return
    *   the integer value represented by the identifier.
    */
  extension (id: PoiId) def value: Int = id

/** Point of interest located inside the simulation space.
  *
  * @param id
  *   unique identifier of the point of interest.
  * @param name
  *   name of the point of interest.
  * @param position
  *   position of the point of interest.
  * @param radius
  *   radius of the point-of-interest area.
  * @param activationDelay
  *   number of ticks required before the point of interest is activated.
  */
case class POI(id: PoiId, name: String, position: P2d, radius: Double, activationDelay: Int = 0):

  require(radius > 0, "Radius must be positive")
  require(activationDelay >= 0, "Activation delay cannot be negative")

  /** Checks whether a position is contained inside the point-of-interest area.
    *
    * @param position
    *   position to check.
    * @return
    *   true if the position is inside or on the boundary of the area, false otherwise.
    */
  def contains(position: P2d): Boolean = (position - this.position).length <= radius

/** Stores the number of consecutive ticks spent by an agent inside each point of interest.
  *
  * @param perPoi
  *   map associating each point-of-interest identifier with its current number of ticks.
  */
case class Residency(perPoi: Map[PoiId, Int]):

  /** Increases by one the number of ticks associated with a point of interest.
    *
    * @param poi
    *   point-of-interest identifier to update.
    * @return
    *   a new residency containing the updated tick count.
    */
  def tickFor(poi: PoiId): Residency = copy(perPoi = perPoi.updatedWith(poi)(t => Some(t.getOrElse(0) + 1)))

  /** Removes the residency information associated with a point of interest.
    *
    * @param poi
    *   point-of-interest identifier to reset.
    * @return
    *   a new residency without the specified point of interest.
    */
  def reset(poi: PoiId): Residency = copy(perPoi = perPoi - poi)

  /** Returns the number of ticks associated with a point of interest.
    *
    * @param poi
    *   point-of-interest identifier to inspect.
    * @return
    *   the number of ticks spent inside the point of interest, or zero if no residency information is available.
    */
  def ticksIn(poi: PoiId): Int = perPoi.getOrElse(poi, 0)

/** Factory and default values for [[Residency]].
  */
object Residency:

  /** Empty residency containing no point-of-interest information.
    */
  val empty: Residency = Residency(Map.empty)
