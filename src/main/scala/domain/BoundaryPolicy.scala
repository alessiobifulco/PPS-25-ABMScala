package domain

trait BoundaryPolicy:

  def apply(candidatePosition: P2d, currentVelocity: V2d, space: Space): (P2d, V2d)

case object BouncePolicy extends BoundaryPolicy:

  override def apply(candidatePosition: P2d, currentVelocity: V2d, space: Space): (P2d, V2d) = space
    .bounce(candidatePosition, currentVelocity)

object BoundaryPolicy:

  val bounce: BoundaryPolicy = BouncePolicy
