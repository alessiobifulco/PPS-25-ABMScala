package domain

/** Geometric shape used to represent the boundaries of a space.
  */
enum Shape:

  /** Rectangular shape defined by its top-left corner, width and height.
    *
    * @param topLeft
    *   top-left position of the rectangle.
    * @param width
    *   width of the rectangle.
    * @param height
    *   height of the rectangle.
    */
  case Rectangle(topLeft: P2d, width: Double, height: Double)

  /** Circular shape defined by its center and radius.
    *
    * @param center
    *   center position of the circle.
    * @param radius
    *   radius of the circle.
    */
  case Circle(center: P2d, radius: Double)
