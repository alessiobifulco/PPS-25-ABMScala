package domain

final case class Bounds(width: Double, height: Double) extends Space:

  private val rectangularSpace = RectangularSpace(width, height)

  override def contains(position: P2d): Boolean =
    rectangularSpace.contains(position)

  override def clamp(position: P2d): P2d =
    rectangularSpace.clamp(position)

  override def bounce(position: P2d, velocity: V2d): (P2d, V2d) =
    rectangularSpace.bounce(position, velocity)
