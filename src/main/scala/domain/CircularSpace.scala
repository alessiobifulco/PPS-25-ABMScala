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
    val correctedPosition = clamp(position)

    if distance == 0 then (correctedPosition, velocity)
    else
      val outwardNormal = V2d(x = offset.x / distance, y = offset.y / distance)
      val velocityAlongNormal = velocity.x * outwardNormal.x + velocity.y * outwardNormal.y
      val shouldBounce = distance > radius || (distance >= radius && velocityAlongNormal > 0)
      val correctedVelocity =
        if shouldBounce then
          V2d(
            x = velocity.x - 2 * velocityAlongNormal * outwardNormal.x,
            y = velocity.y - 2 * velocityAlongNormal * outwardNormal.y
          )
        else velocity

      (correctedPosition, correctedVelocity)

  override def wrap(position: P2d): P2d =
    throw UnsupportedOperationException("WrapPolicy is not defined for CircularSpace")

  override def stop(position: P2d, velocity: V2d): (P2d, V2d) =
    val offset = relativeToCenter(position)
    val distance = offset.length
    val isOutside = distance > radius
    val isMovingOutward =
      if distance == 0 then false
      else
        val outwardNormal = V2d(x = offset.x / distance, y = offset.y / distance)
        velocity.x * outwardNormal.x + velocity.y * outwardNormal.y > 0

    val correctedPosition = clamp(position)
    val correctedVelocity = if isOutside || isMovingOutward then V2d.zero else velocity

    (correctedPosition, correctedVelocity)

  private def relativeToCenter(position: P2d): V2d = V2d(x = position.x - center.x, y = position.y - center.y)
