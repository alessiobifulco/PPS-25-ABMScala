package dsl

import domain.*
import engine.SimulationConfig

object Simulation:
  export ConditionalBehaviour.*
  export CompositeBehaviour.*
  export DiscreteRules.*
  export ContinuousRules.*
  export ChoicesBuilder.behaviour
  export RulesBuilder.rules
  export Chance.chance

  def of[S](config: SimulationBuilder[S] ?=> Unit): SimulationConfig[S] =
    val builder = SimulationBuilder[S]()
    config(using builder)
    builder.build()

  def space[S](s: Space, boundary: BoundaryPolicy)(using builder: SimulationBuilder[S]): Unit = builder
    .setSpace(s, boundary)

  def perception[S](radius: Double)(using builder: SimulationBuilder[S]): Unit = builder.setPerceptionRadius(radius)

  def population[S](size: Int, generator: Int => S)(using builder: SimulationBuilder[S]): Unit =
    builder.setPopulationSize(size)
    builder.setStateGenerator(generator)

  def handleActionsWith[S](handler: ActionHandler[S])(using builder: SimulationBuilder[S]): Unit = builder
    .setActionHandler(handler)

  def choice[S](c: Choice[S])(using builder: ChoicesBuilder[S]): Unit = builder.addChoice(c)

  def rule[S](r: InteractionRule[S])(using builder: RulesBuilder[S]): Unit = builder.addRule(r)
