package dsl

import domain.*

trait ChoicesBuilder[S]:
  def addChoice(choice: Choice[S]): Unit
  def setDefault(actions: AgentContext[S] => List[Action[S]]): Unit
  def build(): List[Choice[S]]

object ChoicesBuilder:

  def apply[S](): ChoicesBuilder[S] = ChoicesBuilderImpl[S]()

  def behaviour[S](block: ChoicesBuilder[S] ?=> Unit)(using simBuilder: SimulationBuilder[S]): Unit =
    val builder = ChoicesBuilder[S]()
    block(using builder)
    builder.build().foreach(simBuilder.addChoice)

  private class ChoicesBuilderImpl[S] extends ChoicesBuilder[S]:

    private var choices: List[Choice[S]] = List.empty[Choice[S]]
    private var default: Option[Choice[S]] = Option.empty[Choice[S]]

    override def addChoice(choice: Choice[S]): Unit = choices = choices :+ choice

    override def setDefault(actions: AgentContext[S] => List[Action[S]]): Unit =
      default = Some(Choice(_ => true, actions))

    override def build(): List[Choice[S]] = default match
      case Some(choice) => choices :+ choice
      case _            => choices
