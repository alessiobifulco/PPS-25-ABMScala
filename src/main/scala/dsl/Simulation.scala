package dsl

import engine.SimulationConfig
import domain.*

object Simulation:
  export BoundaryPolicy.{bounce, stop, wrap}
  export Chance.chance
  export EnvironmentBuilder.{environment, space, population, perception, memory, poi}
  export BehaviorsBuilder.behavior
  export RulesBuilder.rules
  export ConditionalBehavior.*
  export CompositeBehavior.*
  export DiscreteRules.*
  export ContinuousRules.*

  def of[S](config: SimulationBuilder[S] ?=> Unit): SimulationConfig[S] =
    val builder = SimulationBuilder[S]()
    config(using builder)
    builder.build()
