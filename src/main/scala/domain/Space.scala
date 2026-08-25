package domain

/** Space in which agents and points of interest are located.
  */
trait Space:

  /** Checks whether a position is contained inside the space.
    *
    * @param position
    *   position to check.
    * @return
    *   true if the position is contained in the space, false otherwise.
    */
  def contains(position: P2d): Boolean

  /** Corrects a position by moving it inside the space boundaries.
    *
    * @param position
    *   position to clamp.
    * @return
    *   a position contained inside the space.
    */
  def clamp(position: P2d): P2d

  /** Applies a bounce behaviour to a position and velocity.
    *
    * @param position
    *   current position of the entity.
    * @param velocity
    *   current velocity of the entity.
    * @return
    *   a tuple containing the corrected position and velocity.
    */
  def bounce(position: P2d, velocity: V2d): (P2d, V2d)

  /** Applies a stop behaviour to a position and velocity.
    *
    * @param position
    *   current position of the entity.
    * @param velocity
    *   current velocity of the entity.
    * @return
    *   a tuple containing the corrected position and velocity.
    */
  def stop(position: P2d, velocity: V2d): (P2d, V2d)

  /** Generates a random position contained inside the space.
    *
    * @return
    *   a random position inside the space.
    */
  def randomPosition: P2d

  /** Returns the geometric shape representing the space.
    *
    * @return
    *   the shape associated with the space.
    */
  def shape: Shape

/** Space that supports wrapping positions across its boundaries.
  */
trait Toroidal:

  /** Wraps a position according to the topology of the space.
    *
    * @param position
    *   position to wrap.
    * @return
    *   the wrapped position.
    */
  def wrap(position: P2d): P2d
