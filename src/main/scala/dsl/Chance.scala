package dsl

/** Represents a mathematical probability within the DSL. Defined as an opaque type over `Double` so that the only way
  * to obtain a value is the smart constructor below, which confines it to the [0, 1] range without introducing runtime
  * overhead.
  */
opaque type Chance = Double

object Chance:

  /** Wraps a raw double into a [[Chance]], verifying that it falls within the valid [0, 1] range.
    *
    * @param probability
    *   A value between 0.0 (impossible) and 1.0 (certain).
    * @return
    *   The strongly-typed [[Chance]].
    * @throws IllegalArgumentException
    *   if the given probability lies outside the [0, 1] range.
    */
  def apply(probability: Double): Chance =
    require(probability >= 0.0 && probability <= 1.0, "Probability must be between 0 and 1")
    probability

  /** A DSL helper to fluently define a probability.
    *
    * @param probability
    *   The raw double value to wrap.
    * @return
    *   The defined [[Chance]].
    */
  def chance(probability: Double): Chance = Chance(probability)

  /** Provides capabilities to extract the raw value or to evaluate the probability stochastically.
    */
  extension (c: Chance)

    /** @return
      *   The underlying raw double value.
      */
    def value: Double = c

    /** Stochastically evaluates this chance against a random roll.
      *
      * @return
      *   True if the event successfully occurs based on its probability, false otherwise.
      */
    def happens: Boolean = math.random() < c
