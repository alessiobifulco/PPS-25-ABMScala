package engine

import domain.*

/** Encapsulates all the static parameters, rules, and initial conditions required to bootstrap and execute the
  * simulation. It acts as the foundational blueprint for the [[SimulationEngine]].
  *
  * @param initialEnvironment
  *   The starting layout of the simulated world, including the initial population, points of interest, and boundaries.
  * @param behaviors
  *   The ordered collection of decision-making logics available to the agents: at each tick the first applicable
  *   [[Behavior]] is the one producing the [[Action]]s.
  * @param perceptionRadius
  *   The absolute maximum distance defining the sensory range of all agents.
  * @param rules
  *   The ordered set of [[InteractionRule]]s dictating how agents' internal states evolve: at each tick only the first
  *   applicable one is fired.
  * @param neighborStrategy
  *   The algorithmic approach used to discover nearby agents, allowing for spatial query optimizations.
  * @tparam S
  *   The generic type representing the internal state of the Agents.
  */
case class SimulationConfig[S](
    initialEnvironment: Environment[S],
    behaviors: List[Behavior[S]],
    perceptionRadius: Double,
    rules: List[InteractionRule[S]] = List.empty,
    neighborStrategy: NeighborStrategy[S] = NeighborStrategy.bruteForce[S]
)
