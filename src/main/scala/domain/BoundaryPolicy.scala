package domain

trait BoundaryPolicy:
  def apply(candidatePosition: P2d, currentVelocity: V2d, space: Space): (P2d, V2d)

case object BouncePolicy extends BoundaryPolicy:
  override def apply(position: P2d, velocity: V2d, space: Space): (P2d, V2d) = space.bounce(position, velocity)

case object StopPolicy extends BoundaryPolicy:
  override def apply(position: P2d, velocity: V2d, space: Space): (P2d, V2d) = space.stop(position, velocity)

case object WrapPolicy extends BoundaryPolicy:
  override def apply(position: P2d, velocity: V2d, space: Space): (P2d, V2d) = space match
    case toroidal: Toroidal => (toroidal.wrap(position), velocity)
    case other              => other.bounce(position, velocity)

object BoundaryPolicy:
  val bounce: BoundaryPolicy = BouncePolicy
  val wrap: BoundaryPolicy = WrapPolicy
  val stop: BoundaryPolicy = StopPolicy
