package dsl

import domain.*

trait BehaviorsBuilder[S]:
  def add(behavior: Behavior[S]): Unit
  def behaviors: List[Behavior[S]]

object BehaviorsBuilder:

  def apply[S](): BehaviorsBuilder[S] = BehaviorsBuilderImpl[S]()

  def behavior[S](block: BehaviorsBuilder[S] ?=> Unit)(using simBuilder: SimulationBuilder[S]): Unit =
    val builder = BehaviorsBuilder[S]()
    block(using builder)
    builder.behaviors.sortBy(_.whenState.isEmpty).foreach(simBuilder.addBehavior)

  private class BehaviorsBuilderImpl[S] extends BehaviorsBuilder[S]:

    private var added: List[Behavior[S]] = List.empty

    override def add(behavior: Behavior[S]): Unit = added = added :+ behavior

    override def behaviors: List[Behavior[S]] = added
