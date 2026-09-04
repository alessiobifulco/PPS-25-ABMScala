package domain

/** Represents a geometric point in a 2-dimensional space. It is typically used to denote the absolute position of an
  * entity within the environment.
  *
  * @param x
  *   The coordinate on the x-axis.
  * @param y
  *   The coordinate on the y-axis.
  */
case class P2d(x: Double, y: Double)

object P2d:

  extension (p: P2d)

    /** Displaces the point by a given vector, returning a new resulting position.
      *
      * @param v
      *   The vector ([[V2d]]) representing the physical displacement.
      * @return
      *   A new [[P2d]] translated by the vector's components.
      */
    def +(v: V2d): P2d = P2d(p.x + v.x, p.y + v.y)

    /** Calculates the vector resulting from the difference between two points.
      *
      * @param other
      *   The starting point.
      * @return
      *   A [[V2d]] representing the exact direction and distance from `other` to this point.
      */
    def -(other: P2d): V2d = V2d(p.x - other.x, p.y - other.y)
