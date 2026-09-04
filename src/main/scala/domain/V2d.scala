package domain

/** Represents a geometric vector in a 2-dimensional space. It is typically used to denote velocities, movement
  * directions, or forces applied to an entity.
  *
  * @param x
  *   The vector component along the x-axis.
  * @param y
  *   The vector component along the y-axis.
  */
case class V2d(x: Double, y: Double)

object V2d:

  /** The zero vector (0, 0), representing the absence of movement or magnitude.
    */
  val zero: V2d = V2d(0, 0)

  /** Generates a random unit vector (length equal to 1.0) pointing in a random direction.
    *
    * @return
    *   A randomly directed, normalized [[V2d]].
    */
  def random(): V2d =
    val angle = math.random() * 2 * math.Pi
    V2d(math.cos(angle), math.sin(angle))

  extension (v: V2d)

    /** Adds two vectors together component by component, useful for calculating resultant forces or velocities.
      *
      * @param other
      *   The vector to add.
      * @return
      *   A new [[V2d]] representing the sum.
      */
    def +(other: V2d): V2d = V2d(v.x + other.x, v.y + other.y)

    /** Scales the vector by a given scalar multiplier (e.g., to increase or decrease speed).
      *
      * @param factor
      *   The scaling factor.
      * @return
      *   A new scaled [[V2d]].
      */
    def *(factor: Double): V2d = V2d(v.x * factor, v.y * factor)

    /** Calculates the magnitude (or physical length) of the vector.
      *
      * @return
      *   The scalar length of the vector.
      */
    def length: Double = math.sqrt(v.x * v.x + v.y * v.y)

    /** Returns a vector with the same direction but a magnitude exactly equal to 1.0. If applied to a zero vector, it
      * safely returns [[V2d.zero]] to avoid division by zero.
      *
      * @return
      *   The normalized [[V2d]].
      */
    def normalized: V2d = v.length match
      case 0 => V2d.zero
      case l => V2d(v.x / l, v.y / l)
