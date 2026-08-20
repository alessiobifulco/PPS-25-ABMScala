package dsl

import domain.*
import engine.SimulationConfig

object Simulation:
  export BoundaryPolicy.{bounce, stop, wrap}
  export Chance.chance
  export EnvironmentBuilder.{environment, space, population, perception, memory, poi}
  export ChoicesBuilder.behaviour
  export RulesBuilder.rules
  export ConditionalBehaviour.*
  export CompositeBehaviour.*
  export DiscreteRules.*
  export ContinuousRules.*

  def of[S](config: SimulationBuilder[S] ?=> Unit): SimulationConfig[S] =
    val builder = SimulationBuilder[S]()
    config(using builder)
    builder.build()

  def handleActionsWith[S](handler: ActionHandler[S])(using builder: SimulationBuilder[S]): Unit = builder
    .setActionHandler(handler)

  def choice[S](c: Choice[S])(using builder: ChoicesBuilder[S]): Unit = builder.addChoice(c)

  def rule[S](r: InteractionRule[S])(using builder: RulesBuilder[S]): Unit = builder.addRule(r)
