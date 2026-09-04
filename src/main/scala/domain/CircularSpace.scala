package domain

/** Circular space defined by a center point and a positive radius.
  *
  * @param center
  *   center point of the circular space.
  * @param radius
  *   radius of the circular space.
  */
final case class CircularSpace(center: P2d, radius: Double) extends Space, Toroidal:

  require(radius > 0, "Radius must be positive")

  /** Checks whether the input position is inside the circular space.
    *
    * @param position
    *   position to check.
    * @return
    *   true if the position is inside or on the boundary of the space, false otherwise.
    */
  override def contains(position: P2d): Boolean =
    val offset = relativeToCenter(position)
    offset.x * offset.x + offset.y * offset.y <= radius * radius

  /** Clamps a position to the circular space by moving positions outside the boundary onto the circumference.
    *
    * @param position
    *   position to clamp.
    * @return
    *   the input position if it is inside the space, otherwise the closest position on the boundary.
    */
  override def clamp(position: P2d): P2d =
    val offset = relativeToCenter(position)
    val distance = offset.length
    if distance <= radius then position
    else P2d(x = center.x + offset.x / distance * radius, y = center.y + offset.y / distance * radius)

  /** Applies a bounce boundary policy to the input position and velocity.
    *
    * @param position
    *   current position of the entity.
    * @param velocity
    *   current velocity of the entity.
    * @return
    *   a tuple containing the corrected position and velocity.
    */
  override def bounce(position: P2d, velocity: V2d): (P2d, V2d) =
    val offset = relativeToCenter(position)
    val distance = offset.length
    if distance == 0 then (clamp(position), velocity)
    else
      val normal = V2d(x = offset.x / distance, y = offset.y / distance)
      val alongNormal = velocity.x * normal.x + velocity.y * normal.y
      val shouldBounce = distance > radius || (distance >= radius && alongNormal > 0)
      val correctedVelocity =
        if shouldBounce then V2d(velocity.x - 2 * alongNormal * normal.x, velocity.y - 2 * alongNormal * normal.y)
        else velocity
      (clamp(position), correctedVelocity)

  /** Applies a wrap boundary policy to the input position.
    *
    * @param position
    *   position to wrap.
    * @return
    *   the input position if it is inside the space, otherwise the corresponding position on the opposite side of the
    *   circumference.
    */
  override def wrap(position: P2d): P2d =
    val offset = relativeToCenter(position)
    if offset.length <= radius then position
    else
      val normalizedOffset = offset.normalized
      P2d(center.x - normalizedOffset.x * radius, center.y - normalizedOffset.y * radius)

  /** Applies a stop boundary policy to the input position and velocity.
    *
    * @param position
    *   current position of the entity.
    * @param velocity
    *   current velocity of the entity.
    * @return
    *   a tuple containing the corrected position and velocity.
    */
  override def stop(position: P2d, velocity: V2d): (P2d, V2d) =
    val offset = relativeToCenter(position)
    val distance = offset.length
    val isMovingOutward = distance > 0 && (velocity.x * offset.x / distance + velocity.y * offset.y / distance) > 0
    (clamp(position), if distance > radius || isMovingOutward then V2d.zero else velocity)

  /** Generates a random position inside the circular space.
    *
    * @return
    *   a random position contained in the circular space.
    */
  override def randomPosition: P2d =
    val angle = math.random() * 2 * math.Pi
    val distance = radius * math.sqrt(math.random())
    P2d(x = center.x + distance * math.cos(angle), y = center.y + distance * math.sin(angle))

  /** Returns the geometric shape representing this space.
    *
    * @return
    *   a circular shape with this space's center and radius.
    */
  override def shape: Shape = Shape.Circle(center, radius)

  /** Calculates the vector from the center of the space to the input position.
    *
    * @param position
    *   position from which to calculate the relative vector.
    * @return
    *   the vector from the center to the input position.
    */
  private def relativeToCenter(position: P2d): V2d = V2d(x = position.x - center.x, y = position.y - center.y)
