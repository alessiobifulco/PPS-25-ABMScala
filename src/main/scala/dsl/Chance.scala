package dsl

opaque type Chance = Double

object Chance:

  def apply(probability: Double): Chance =
    require(probability >= 0.0 && probability <= 1.0, "Probability must be between 0 and 1")
    probability

  def chance(probability: Double): Chance = Chance(probability)

  extension (c: Chance)
    def value: Double = c
    def happens: Boolean = math.random() < c
