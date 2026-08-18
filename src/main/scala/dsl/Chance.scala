package dsl

case class Chance(probability: Double):
  require(probability >= 0.0 && probability <= 1.0, "Probability must be between 0 and 1")

  def happens: Boolean = math.random() < probability

object Chance:
  def chance(probability: Double): Chance = Chance(probability)
