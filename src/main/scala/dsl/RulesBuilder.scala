package dsl

import domain.*

/** A mutable builder used within the DSL context to accumulate a collection of [[InteractionRule]]s. It provides the
  * foundational scope for defining how agents' internal states transition over time based on their interactions and
  * surroundings.
  *
  * @tparam S
  *   The generic type representing the internal state of the Agent.
  */
trait RulesBuilder[S]:

  /** Registers a new interaction rule into the builder's internal collection.
    *
    * @param rule
    *   The [[InteractionRule]] to be added.
    */
  def add(rule: InteractionRule[S]): Unit

  /** Retrieves all the state transition rules accumulated so far.
    *
    * @return
    *   A list of the defined [[InteractionRule]]s.
    */
  def rules: List[InteractionRule[S]]

object RulesBuilder:

  /** Instantiates a new, empty [[RulesBuilder]].
    */
  def apply[S](): RulesBuilder[S] = RulesBuilderImpl[S]()

  /** A DSL entry point for grouping and defining state transition rules. It evaluates the provided block within a
    * scoped [[RulesBuilder]] context and automatically registers all the collected rules into the overarching
    * [[SimulationBuilder]], preserving their declaration order, which matters because the engine fires only the first
    * applicable rule.
    *
    * @param block
    *   A context function containing the DSL declarations for the interaction rules.
    * @param simBuilder
    *   The implicit parent builder where the rules will ultimately be stored.
    */
  def rules[S](block: RulesBuilder[S] ?=> Unit)(using simBuilder: SimulationBuilder[S]): Unit =
    val builder = RulesBuilder[S]()
    block(using builder)
    builder.rules.foreach(simBuilder.addRule)

  private class RulesBuilderImpl[S] extends RulesBuilder[S]:

    private var added: List[InteractionRule[S]] = List.empty

    override def add(rule: InteractionRule[S]): Unit = added = added :+ rule

    override def rules: List[InteractionRule[S]] = added
