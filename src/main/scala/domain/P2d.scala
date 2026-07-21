package domain

case class P2d(x: Double, y: Double)

object P2d:
  extension (p: P2d)
    def +(v: V2d): P2d = P2d(p.x + v.x, p.y + v.y)
    def -(other: P2d): V2d = V2d(p.x - other.x, p.y - other.y)