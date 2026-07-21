package domain

/** An absolute two-dimensional position, distinct from [[V2d]], which represents a displacement or direction rather
  * than a location.
  *
  * @param x
  *   the horizontal coordinate.
  * @param y
  *   the vertical coordinate.
  */
case class P2d(x: Double, y: Double)

/** Utilities for [[P2d]] positions.
  */
object P2d:

  extension (p: P2d)

    /** Translates this position by the given vector.
      *
      * @param v
      *   the displacement to apply.
      * @return
      *   the resulting position.
      */
    def +(v: V2d): P2d = P2d(p.x + v.x, p.y + v.y)

    /** Computes the vector pointing from `other` to this position.
      *
      * @param other
      *   the position to subtract.
      * @return
      *   the vector difference between this position and `other`.
      */
    def -(other: P2d): V2d = V2d(p.x - other.x, p.y - other.y)
