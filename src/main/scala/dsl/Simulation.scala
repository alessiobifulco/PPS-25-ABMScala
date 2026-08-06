package dsl

import domain.*
import engine.SimulationConfig

object Simulation:

  def of[S](config: SimulationBuilder[S] ?=> Unit): SimulationConfig[S] =
    given builder: SimulationBuilder[S] = SimulationBuilder[S]()
    config(using builder)
    builder.build()

  def space[S](space: Space, boundary: BoundaryPolicy)(using builder: SimulationBuilder[S]): Unit =
    builder.space(space, boundary)

  def perception[S](radius: Double)(using builder: SimulationBuilder[S]): Unit =
    builder.perception(radius)

  def population[S](size: Int, generator: Int => S)(using builder: SimulationBuilder[S]): Unit =
    builder.population(size, generator)

  def choice[S](c: Choice[S])(using builder: SimulationBuilder[S]): Unit =
    builder.choice(c)

  def rule[S](r: InteractionRule[S])(using builder: SimulationBuilder[S]): Unit =
    builder.rule(r)