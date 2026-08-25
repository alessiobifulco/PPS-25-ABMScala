package domain

/** Rectangular space defined by a positive width and height.
  *
  * @param width
  *   width of the rectangular space.
  * @param height
  *   height of the rectangular space.
  */
final case class RectangularSpace(width: Double, height: Double) extends Space, Toroidal:

  require(width > 0, "Width must be positive")

  require(height > 0, "Height must be positive")

  /** Checks whether the input position is inside the rectangular space.
    *
    * @param position
    *   position to check.
    * @return
    *   true if the position is inside or on the boundary of the space, false otherwise.
    */
  override def contains(position: P2d): Boolean = position.x >= 0 && position.x <= width && position.y >= 0 &&
    position.y <= height

  /** Clamps a position to the boundaries of the rectangular space.
    *
    * @param position
    *   position to clamp.
    * @return
    *   a position contained inside the rectangular space.
    */
  override def clamp(position: P2d): P2d = P2d(x = position.x.max(0).min(width), y = position.y.max(0).min(height))

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
    val shouldBounceX = (position.x <= 0 && velocity.x < 0) || (position.x >= width && velocity.x > 0)
    val shouldBounceY = (position.y <= 0 && velocity.y < 0) || (position.y >= height && velocity.y > 0)
    val correctedVelocity =
      V2d(x = if shouldBounceX then -velocity.x else velocity.x, y = if shouldBounceY then -velocity.y else velocity.y)
    (clamp(position), correctedVelocity)

  /** Applies a wrap boundary policy to the input position.
    *
    * @param position
    *   position to wrap.
    * @return
    *   a position wrapped inside the rectangular space.
    */
  override def wrap(position: P2d): P2d =
    P2d(x = wrapCoordinate(position.x, width), y = wrapCoordinate(position.y, height))

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
    val isOutside = !contains(position)
    val isMovingOutward =
      (position.x <= 0 && velocity.x < 0) ||
        (position.x >= width && velocity.x > 0) ||
        (position.y <= 0 && velocity.y < 0) ||
        (position.y >= height && velocity.y > 0)
    (clamp(position), if isOutside || isMovingOutward then V2d.zero else velocity)

  /** Generates a random position inside the rectangular space.
    *
    * @return
    *   a random position contained in the rectangular space.
    */
  override def randomPosition: P2d = P2d(math.random() * width, math.random() * height)

  /** Returns the geometric shape representing this space.
    *
    * @return
    *   a rectangular shape with origin at `(0, 0)`, the specified width and the specified height.
    */
  override def shape: Shape = Shape.Rectangle(P2d(0, 0), width, height)

  /** Wraps a coordinate inside the interval defined by the specified size.
    *
    * @param value
    *   coordinate to wrap.
    * @param size
    *   size of the interval.
    * @return
    *   the wrapped coordinate.
    */
  private def wrapCoordinate(value: Double, size: Double): Double =
    val remainder = value % size
    if remainder < 0 then remainder + size else remainder
