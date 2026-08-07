package dsl

import domain.*
import engine.SimulationConfig

object Simulation:
  export ConditionalBehaviour.*
  export CompositeBehaviour.*
  export DiscreteRules.*
  export ContinuousRules.*

  def of[S](config: SimulationBuilder[S] ?=> Unit): SimulationConfig[S] =
    given builder: SimulationBuilder[S] = SimulationBuilder[S]()
    config(using builder)
    builder.build()

  def space[S](space: Space, boundary: BoundaryPolicy)(using builder: SimulationBuilder[S]): Unit = builder
    .setSpace(space, boundary)

  def perception[S](radius: Double)(using builder: SimulationBuilder[S]): Unit = builder.setPerceptionRadius(radius)

  def population[S](size: Int, generator: Int => S)(using builder: SimulationBuilder[S]): Unit =
    builder.setPopulationSize(size)
    builder.setStateGenerator(generator)

  def choice[S](c: Choice[S])(using builder: SimulationBuilder[S]): Unit = builder.addChoice(c)

  def rule[S](r: InteractionRule[S])(using builder: SimulationBuilder[S]): Unit = builder.addRule(r)
