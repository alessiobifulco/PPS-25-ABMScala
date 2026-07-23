package domain

final case class CircularSpace(center: P2d, radius: Double) extends Space:
  require(radius > 0, "Radius must be positive")

  override def contains(position: P2d): Boolean =
    val offset = relativeToCenter(position)
    offset.x * offset.x + offset.y * offset.y <= radius * radius

  override def clamp(position: P2d): P2d =
    val offset = relativeToCenter(position)
    val distance = offset.length
    if distance <= radius then position
    else P2d(x = center.x + offset.x / distance * radius, y = center.y + offset.y / distance * radius)

  override def bounce(position: P2d, velocity: V2d): (P2d, V2d) =
    val offset = relativeToCenter(position)
    val distance = offset.length
    if distance == 0 then (clamp(position), velocity)
    else
      val normal = V2d(x = offset.x / distance, y = offset.y / distance)
      val alongNormal = velocity.x * normal.x + velocity.y * normal.y
      val shouldBounce = distance >= radius && alongNormal > 0 || distance > radius
      val correctedVelocity =
        if shouldBounce then V2d(velocity.x - 2 * alongNormal * normal.x, velocity.y - 2 * alongNormal * normal.y)
        else velocity
      (clamp(position), correctedVelocity)

  override def stop(position: P2d, velocity: V2d): (P2d, V2d) =
    val offset = relativeToCenter(position)
    val distance = offset.length
    val isMovingOutward = distance > 0 && (velocity.x * offset.x / distance + velocity.y * offset.y / distance) > 0
    (clamp(position), if distance > radius || isMovingOutward then V2d.zero else velocity)

  private def relativeToCenter(position: P2d): V2d = V2d(x = position.x - center.x, y = position.y - center.y)
