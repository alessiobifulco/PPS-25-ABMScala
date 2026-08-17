package dsl

import domain.*

trait RuleBuilder[S]:
  def build(): InteractionRule[S]

trait RulesBuilder[S]:
  def addRuleBuilder(rb: RuleBuilder[S]): Unit
  def addRule(rule: InteractionRule[S]): Unit
  def build(): List[InteractionRule[S]]

object RulesBuilder:

  def apply[S](): RulesBuilder[S] = RulesBuilderImpl[S]()

  def rules[S](block: RulesBuilder[S] ?=> Unit)(using simBuilder: SimulationBuilder[S]): Unit =
    val builder = RulesBuilder[S]()
    block(using builder)
    builder.build().foreach(simBuilder.addRule)

  private class RulesBuilderImpl[S] extends RulesBuilder[S]:

    private var ruleBuilders: List[RuleBuilder[S]] = List.empty[RuleBuilder[S]]

    override def addRuleBuilder(rb: RuleBuilder[S]): Unit = ruleBuilders = ruleBuilders :+ rb

    override def addRule(rule: InteractionRule[S]): Unit = addRuleBuilder(() => rule)

    override def build(): List[InteractionRule[S]] = ruleBuilders.map(_.build())
