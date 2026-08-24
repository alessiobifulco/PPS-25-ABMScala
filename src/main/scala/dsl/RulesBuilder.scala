package dsl

import domain.*

trait RulesBuilder[S]:
  def add(rule: InteractionRule[S]): Unit
  def rules: List[InteractionRule[S]]

object RulesBuilder:

  def apply[S](): RulesBuilder[S] = RulesBuilderImpl[S]()

  def rules[S](block: RulesBuilder[S] ?=> Unit)(using simBuilder: SimulationBuilder[S]): Unit =
    val builder = RulesBuilder[S]()
    block(using builder)
    builder.rules.foreach(simBuilder.addRule)

  private class RulesBuilderImpl[S] extends RulesBuilder[S]:

    private var added: List[InteractionRule[S]] = List.empty

    override def add(rule: InteractionRule[S]): Unit = added = added :+ rule

    override def rules: List[InteractionRule[S]] = added
