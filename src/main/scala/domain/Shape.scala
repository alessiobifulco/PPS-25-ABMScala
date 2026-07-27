package domain

enum Shape:
  case Rectangle(topLeft: P2d, width: Double, height: Double)
  case Circle(center: P2d, radius: Double)
