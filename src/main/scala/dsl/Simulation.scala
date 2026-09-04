package dsl

import engine.SimulationConfig
import domain.*

/** The central facade and main entry point of the DSL. By importing the members of this object (e.g.,
  * `import dsl.Simulation.*`), users gain immediate access to the entire declarative vocabulary required to model and
  * construct a simulation (environment setup, behaviors, rules, and conditions).
  */
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

  /** The root DSL block used to define and instantiate a complete simulation setup. It evaluates the provided
    * configuration block within a scoped [[SimulationBuilder]] context, automatically resolving it into a ready-to-run
    * [[SimulationConfig]].
    *
    * @param config
    *   A context function containing all the top-level DSL declarations for the simulation.
    * @tparam S
    *   The generic type representing the internal state of the Agents.
    * @return
    *   The finalized [[SimulationConfig]] ready to be fed to the simulation engine.
    * @throws IllegalArgumentException
    *   if the block does not declare any environment.
    */
  def of[S](config: SimulationBuilder[S] ?=> Unit): SimulationConfig[S] =
    val builder = SimulationBuilder[S]()
    config(using builder)
    builder.build()
