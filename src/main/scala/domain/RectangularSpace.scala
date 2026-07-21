package domain

final case class RectangularSpace(width: Double, height: Double) extends Space:

  require(width > 0, "Width must be positive")
  require(height > 0, "Height must be positive")

  override def contains(position: P2d): Boolean = position.x >= 0 && position.x <= width && position.y >= 0 &&
    position.y <= height

  override def clamp(position: P2d): P2d = P2d(x = position.x.max(0).min(width), y = position.y.max(0).min(height))

  override def bounce(position: P2d, velocity: V2d): (P2d, V2d) =
    val shouldBounceX = (position.x <= 0 && velocity.x < 0) || (position.x >= width && velocity.x > 0)
    val shouldBounceY = (position.y <= 0 && velocity.y < 0) || (position.y >= height && velocity.y > 0)
    val correctedPosition = clamp(position)
    val correctedVelocity =
      V2d(x = if shouldBounceX then -velocity.x else velocity.x, y = if shouldBounceY then -velocity.y else velocity.y)
    (correctedPosition, correctedVelocity)
