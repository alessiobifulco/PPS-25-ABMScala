package domain

trait BoundaryPolicy:

  def apply(candidatePosition: P2d, currentVelocity: V2d, space: Space): (P2d, V2d)

case object BouncePolicy extends BoundaryPolicy:

  override def apply(candidatePosition: P2d, currentVelocity: V2d, space: Space): (P2d, V2d) = space
    .bounce(candidatePosition, currentVelocity)

case object WrapPolicy extends BoundaryPolicy:

  override def apply(candidatePosition: P2d, currentVelocity: V2d, space: Space): (P2d, V2d) =
    (space.wrap(candidatePosition), currentVelocity)

case object StopPolicy extends BoundaryPolicy:

  override def apply(candidatePosition: P2d, currentVelocity: V2d, space: Space): (P2d, V2d) = space
    .stop(candidatePosition, currentVelocity)

object BoundaryPolicy:

  val bounce: BoundaryPolicy = BouncePolicy
  val wrap: BoundaryPolicy = WrapPolicy
  val stop: BoundaryPolicy = StopPolicy
