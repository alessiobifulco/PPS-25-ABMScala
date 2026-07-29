package domain

enum BoundaryPolicy:
  case bounce, stop, wrap

  def apply(position: P2d, velocity: V2d, space: Space): (P2d, V2d) = this match
    case BoundaryPolicy.bounce => space.bounce(position, velocity)
    case BoundaryPolicy.stop   => space.stop(position, velocity)
    case BoundaryPolicy.wrap   => space match
        case toroidal: Toroidal => (toroidal.wrap(position), velocity)
        case other              => other.bounce(position, velocity)
