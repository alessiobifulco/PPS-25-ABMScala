package domain

/** A two-dimensional vector, used throughout the simulation to represent an agent's velocity or a generic
  * displacement/direction in space. Distinct from [[P2d]], which represents an absolute position.
  *
  * @param x
  *   the horizontal component.
  * @param y
  *   the vertical component.
  */
case class V2d(x: Double, y: Double)

/** Factory and utilities for [[V2d]] vectors.
  */
object V2d:

  /** The zero vector, representing no velocity or displacement.
    */
  val zero: V2d = V2d(0, 0)

  /** Generates a unit vector pointing in a uniformly random direction. Used as a fallback when an agent has no current
    * velocity to maintain.
    *
    * @return
    *   a vector of length 1, with a random direction.
    */
  def random(): V2d =
    val angle = math.random() * 2 * math.Pi
    V2d(math.cos(angle), math.sin(angle))

  extension (v: V2d)

    /** Sums this vector with `other`, component-wise.
      *
      * @param other
      *   the vector to add.
      * @return
      *   the resulting vector.
      */
    def +(other: V2d): V2d = V2d(v.x + other.x, v.y + other.y)

    /** Scales this vector by `factor`.
      *
      * @param factor
      *   the scaling factor.
      * @return
      *   the resulting vector.
      */
    def *(factor: Double): V2d = V2d(v.x * factor, v.y * factor)

    /** The Euclidean length (magnitude) of this vector.
      *
      * @return
      *   the length of the vector.
      */
    def length: Double = math.sqrt(v.x * v.x + v.y * v.y)

    /** Normalizes this vector to unit length, preserving its direction. The zero vector is mapped to itself, avoiding
      * division by zero.
      *
      * @return
      *   a vector of length 1 with the same direction as this one, or [[V2d.zero]] if this vector is zero.
      */
    def normalized: V2d =
      val l = v.length
      if l == 0 then V2d.zero else V2d(v.x / l, v.y / l)
