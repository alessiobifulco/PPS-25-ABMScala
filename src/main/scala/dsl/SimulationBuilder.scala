package dsl

import domain.*
import engine.SimulationConfig

/** A mutable, fluent builder acting as the top-level aggregator for the simulation DSL. It collects the environmental
  * layout, the agent behaviors, and the interaction rules, ultimately compiling them into a valid, ready-to-run
  * [[SimulationConfig]].
  *
  * @tparam S
  *   The generic type representing the internal state of the Agents.
  */
trait SimulationBuilder[S]:

  /** Registers the spatial and demographic specifications for the simulated world, replacing the previous ones if the
    * environment has already been declared.
    *
    * @param spec
    *   The [[EnvironmentSpec]] defining boundaries, initial population, and points of interest.
    * @return
    *   The builder instance itself for fluent chaining.
    */
  def setEnvironment(spec: EnvironmentSpec[S]): this.type

  /** Appends a decision-making behavior to the simulation, preserving the order of insertion.
    *
    * @param behavior
    *   The [[Behavior]] dictating agent actions.
    * @return
    *   The builder instance itself for fluent chaining.
    */
  def addBehavior(behavior: Behavior[S]): this.type

  /** Appends a state-transition rule to the simulation's rule set, preserving the order of insertion.
    *
    * @param rule
    *   The [[InteractionRule]] dictating how agents' states evolve.
    * @return
    *   The builder instance itself for fluent chaining.
    */
  def addRule(rule: InteractionRule[S]): this.type

  /** Validates the accumulated setup and materializes the final blueprint. This terminal operation actively
    * instantiates the initial [[Agent]] population based on the provided environment specifications, assigning unique
    * IDs, starting positions, random velocities, and a memory of the requested capacity, whenever one has been
    * declared.
    *
    * @return
    *   The fully assembled [[SimulationConfig]] required by the engine.
    * @throws IllegalArgumentException
    *   if the environment specification was not provided.
    */
  def build(): SimulationConfig[S]

object SimulationBuilder:

  /** Instantiates a new, empty [[SimulationBuilder]] for internal DSL usage.
    */
  private[dsl] def apply[S](): SimulationBuilder[S] = SimulationBuilderImpl[S]()

  private class SimulationBuilderImpl[S] extends SimulationBuilder[S]:

    private var environment: Option[EnvironmentSpec[S]] = None
    private var behaviors: List[Behavior[S]] = Nil
    private var rules: List[InteractionRule[S]] = Nil

    override def setEnvironment(spec: EnvironmentSpec[S]): this.type =
      environment = Some(spec)
      this

    override def addBehavior(behavior: Behavior[S]): this.type =
      behaviors = behaviors :+ behavior
      this

    override def addRule(rule: InteractionRule[S]): this.type =
      rules = rules :+ rule
      this

    override def build(): SimulationConfig[S] =
      require(environment.nonEmpty, "Cannot build the simulation: environment is missing")
      val spec = environment.get
      val memory = spec.memoryCapacity.map(Memory.apply)
      val agents = (0 until spec.populationSize).toList
        .map(i => Agent(AgentId(i), spec.positionAt(i), V2d.random(), spec.stateAt(i), memory))

      SimulationConfig(
        Environment(spec.space, agents, spec.boundary, spec.poiList),
        behaviors,
        spec.perceptionRadius,
        rules
      )
