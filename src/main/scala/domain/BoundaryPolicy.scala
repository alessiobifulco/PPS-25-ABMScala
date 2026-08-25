package domain

/** Policy used to manage the behaviour of an entity when it reaches the boundary of the environment.
  */
enum BoundaryPolicy:

  /** Reflects the entity's movement when it reaches the boundary.
    */
  case bounce

  /** Stops the entity when it reaches the boundary.
    */
  case stop

  /** Moves the entity to the opposite side of the environment when it reaches the boundary. If the space is not
    * toroidal, the bounce policy is applied.
    */
  case wrap

  /** Applies the selected boundary policy to the input position and velocity.
    *
    * @param position
    *   current position of the entity.
    * @param velocity
    *   current velocity of the entity.
    * @param space
    *   space in which the entity moves.
    * @return
    *   a tuple containing the updated position and velocity.
    */
  def apply(position: P2d, velocity: V2d, space: Space): (P2d, V2d) = this match
    case BoundaryPolicy.bounce => space.bounce(position, velocity)
    case BoundaryPolicy.stop   => space.stop(position, velocity)
    case BoundaryPolicy.wrap   => space match
        case toroidal: Toroidal => (toroidal.wrap(position), velocity)
        case other              => other.bounce(position, velocity)
