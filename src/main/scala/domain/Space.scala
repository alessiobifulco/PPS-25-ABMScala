package domain

trait Space:

  def contains(position: P2d): Boolean

  def clamp(position: P2d): P2d

  def bounce(position: P2d, velocity: V2d): (P2d, V2d)