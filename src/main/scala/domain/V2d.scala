package domain

case class V2d(x: Double, y: Double)

object V2d:
  val zero: V2d = V2d(0, 0)

  extension (v: V2d)
    def +(other: V2d): V2d = V2d(v.x + other.x, v.y + other.y)
    def *(factor: Double): V2d = V2d(v.x * factor, v.y * factor)
    def length: Double = math.sqrt(v.x * v.x + v.y * v.y)