package dsl

import domain.*

/** A mutable builder used within the DSL context to accumulate a collection of [[Behavior]]s. It provides the
  * foundational scope for defining how agents should act during the simulation.
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

  /** Retrieves all the behaviors accumulated so far.
    *
    * @return
    *   A list of the defined [[Behavior]]s.
    */
  def behaviors: List[Behavior[S]]

object BehaviorsBuilder:

  /** Instantiates a new, empty [[BehaviorsBuilder]].
    */
  def apply[S](): BehaviorsBuilder[S] = BehaviorsBuilderImpl[S]()

  /** A DSL entry point for grouping and defining agent behaviors. It evaluates the provided block within a scoped
    * [[BehaviorsBuilder]] context and automatically registers the collected behaviors into the overarching
    * [[SimulationBuilder]].
    *
    * Note: Before registration, behaviors are automatically sorted so that state-specific behaviors take precedence
    * over universal/fallback ones (i.e., those where `whenState` is empty). The ordering is relevant because the engine
    * fires only the first applicable [[Behavior]] of the list.
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
