package dsl

import domain.*

/** A mutable builder used within the DSL context to accumulate a collection of [[Behavior]]s. It provides the
  * foundational scope for defining how agents should act during the simulation.
  *
  * Mutability is confined to a single declaration block: within the DSL the builder is created by
  * [[BehaviorsBuilder.behavior]], filled while the block runs and then discarded, so the rest of the library only
  * receives an immutable list.
  *
  * @tparam S
  *   The generic type representing the internal state of the Agent.
  */
trait BehaviorsBuilder[S]:

  /** Registers a new behavior into the builder's internal collection.
    *
    * @param behavior
    *   The [[Behavior]] to be added.
    */
  def add(behavior: Behavior[S]): Unit

  /** Retrieves all the behaviors accumulated so far, in declaration order.
    *
    * @return
    *   A list of the defined [[Behavior]]s.
    */
  def behaviors: List[Behavior[S]]

object BehaviorsBuilder:

  /** Instantiates a new, empty [[BehaviorsBuilder]].
    */
  def apply[S](): BehaviorsBuilder[S] = BehaviorsBuilderImpl[S]()

  /** A DSL entry point for declaring agent behaviors. The block is a context function.
    *
    * Once the block has run, the behaviors are registered into the [[SimulationBuilder]], with the state-specific ones
    * before the universal fallbacks (those with an empty `whenState`). The order matters because the engine fires only
    * the first applicable behavior (see [[engine.SimulationEngine.tick]]); since the sort is stable, behaviors of equal
    * specificity keep their declaration order.
    *
    * @param block
    *   A context function containing the DSL declarations for the agent's behaviors.
    * @param simBuilder
    *   The implicit parent builder where the behaviors will ultimately be stored.
    */
  def behavior[S](block: BehaviorsBuilder[S] ?=> Unit)(using simBuilder: SimulationBuilder[S]): Unit =
    val builder = BehaviorsBuilder[S]()
    block(using builder)
    builder.behaviors.sortBy(_.whenState.isEmpty).foreach(simBuilder.addBehavior)

  private class BehaviorsBuilderImpl[S] extends BehaviorsBuilder[S]:

    private var added: List[Behavior[S]] = List.empty

    override def add(behavior: Behavior[S]): Unit = added = added :+ behavior

    override def behaviors: List[Behavior[S]] = added
