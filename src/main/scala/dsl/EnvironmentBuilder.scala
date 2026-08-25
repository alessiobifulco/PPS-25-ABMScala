package dsl

import domain.*

/** Specification containing all the configuration values required to create an environment.
  *
  * @param space
  *   space in which the simulation takes place.
  * @param boundary
  *   boundary policy applied to the agents.
  * @param perceptionRadius
  *   radius used for agent perception.
  * @param populationSize
  *   number of agents in the population.
  * @param stateAt
  *   function used to generate the state of each agent.
  * @param positionAt
  *   function used to determine the position of each agent.
  * @param poiList
  *   points of interest contained in the environment.
  * @param memoryCapacity
  *   optional memory capacity assigned to the agents.
  * @tparam S
  *   state type associated with the agents.
  */
private[dsl] case class EnvironmentSpec[S](
    space: Space,
    boundary: BoundaryPolicy,
    perceptionRadius: Double,
    populationSize: Int,
    stateAt: Int => S,
    positionAt: Int => P2d,
    poiList: List[POI],
    memoryCapacity: Option[Int]
)

/** Builder used to configure an environment through the DSL.
  *
  * @tparam S
  *   state type associated with the agents.
  */
trait EnvironmentBuilder[S]:

  /** Sets the space and boundary policy of the environment.
    *
    * @param space
    *   space in which the simulation takes place.
    * @param boundary
    *   boundary policy applied to the agents.
    */
  def setSpace(space: Space, boundary: BoundaryPolicy): Unit

  /** Sets the radius used for agent perception.
    *
    * @param radius
    *   perception radius.
    */
  def setPerceptionRadius(radius: Double): Unit

  /** Sets the population size and state generator.
    *
    * @param size
    *   number of agents.
    * @param generator
    *   function used to generate each agent's state.
    */
  def setPopulation(size: Int, generator: Int => S): Unit

  /** Sets the function used to determine the position of each agent.
    *
    * @param placement
    *   position generator.
    */
  def setPlacement(placement: Int => P2d): Unit

  /** Sets the memory capacity of the agents.
    *
    * @param capacity
    *   memory capacity.
    */
  def setMemoryCapacity(capacity: Int): Unit

  /** Adds a point of interest to the environment.
    *
    * @param poi
    *   point of interest to add.
    */
  def addPoi(poi: POI): Unit

  /** Builds the environment specification.
    *
    * @return
    *   the configured environment specification.
    */
  def build(): EnvironmentSpec[S]

/** Factory methods and DSL operations for configuring an environment.
  */
object EnvironmentBuilder:

  /** Default boundary policy used when no policy is explicitly specified.
    */
  private val defaultBoundary = BoundaryPolicy.bounce

  /** Creates a new environment builder.
    *
    * @tparam S
    *   state type associated with the agents.
    * @return
    *   a new environment builder.
    */
  def apply[S]: EnvironmentBuilder[S] = EnvironmentBuilderImpl[S]()

  /** Configures an environment and assigns it to the simulation builder.
    *
    * @param block
    *   configuration block executed with an environment builder.
    * @param simBuilder
    *   simulation builder that receives the resulting specification.
    * @tparam S
    *   state type associated with the agents.
    */
  def environment[S](block: EnvironmentBuilder[S] ?=> Unit)(using simBuilder: SimulationBuilder[S]): Unit =
    val builder = EnvironmentBuilder[S]
    block(using builder)
    simBuilder.setEnvironment(builder.build())

  /** Starts the configuration of the environment space.
    *
    * @param s
    *   space in which the simulation takes place.
    * @param builder
    *   environment builder used for the configuration.
    * @tparam S
    *   state type associated with the agents.
    * @return
    *   a configuration object used to specify the boundary policy.
    */
  def space[S](s: Space)(using builder: EnvironmentBuilder[S]): SpaceConfig[S] =
    builder.setSpace(s, defaultBoundary)
    SpaceConfig(s, builder)

  /** Starts the configuration of the agent population.
    *
    * @param size
    *   number of agents.
    * @param builder
    *   environment builder used for the configuration.
    * @tparam S
    *   state type associated with the agents.
    * @return
    *   a configuration object used to specify the agents' state.
    */
  def population[S](size: Int)(using builder: EnvironmentBuilder[S]): PopulationConfig[S] =
    PopulationConfig(size, builder)

  /** Sets the perception radius of the agents.
    *
    * @param radius
    *   perception radius.
    * @param builder
    *   environment builder used for the configuration.
    * @tparam S
    *   state type associated with the agents.
    */
  def perception[S](radius: Double)(using builder: EnvironmentBuilder[S]): Unit = builder.setPerceptionRadius(radius)

  /** Sets the memory capacity of the agents.
    *
    * @param capacity
    *   memory capacity.
    * @param builder
    *   environment builder used for the configuration.
    * @tparam S
    *   state type associated with the agents.
    */
  def memory[S](capacity: Int)(using builder: EnvironmentBuilder[S]): Unit = builder.setMemoryCapacity(capacity)

  /** Adds a point of interest to the environment.
    *
    * @param p
    *   point of interest to add.
    * @param builder
    *   environment builder used for the configuration.
    */
  def poi(p: POI)(using builder: EnvironmentBuilder[?]): Unit = builder.addPoi(p)

  /** Configuration object used to specify the boundary policy of a space.
    *
    * @param s
    *   configured space.
    * @param builder
    *   environment builder used for the configuration.
    * @tparam S
    *   state type associated with the agents.
    */
  final class SpaceConfig[S](s: Space, builder: EnvironmentBuilder[S]):

    /** Sets the boundary policy for the configured space.
      *
      * @param policy
      *   boundary policy to apply.
      */
    infix def withBoundary(policy: BoundaryPolicy): Unit = builder.setSpace(s, policy)

  /** Configuration object used to configure the population.
    *
    * @param size
    *   number of agents.
    * @param builder
    *   environment builder used for the configuration.
    * @tparam S
    *   state type associated with the agents.
    */
  final class PopulationConfig[S](size: Int, builder: EnvironmentBuilder[S]):

    private var current: Option[Int => S] = None

    /** Assigns the same state to every agent.
      *
      * @param state
      *   state assigned to every agent.
      * @return
      *   this population configuration.
      */
    infix def of(state: S): PopulationConfig[S] = generatedBy(_ => state)

    /** Generates the state of every agent from a by-name value.
      *
      * @param state
      *   state generated for each agent.
      * @return
      *   this population configuration.
      */
    infix def eachBeing(state: => S): PopulationConfig[S] = generatedBy(_ => state)

    /** Assigns a specific state to the first agent while preserving the previous generator for the remaining agents.
      *
      * @param state
      *   state assigned to the first agent.
      * @return
      *   this population configuration.
      */
    infix def withOne(state: S): PopulationConfig[S] = current match
      case Some(previous) => generatedBy(agentIndex => if agentIndex == 0 then state else previous(agentIndex))
      case _              => generatedBy(_ => state)

    /** Sets the function used to place the agents.
      *
      * @param placement
      *   position generator.
      * @return
      *   this population configuration.
      */
    infix def placedAt(placement: Int => P2d): PopulationConfig[S] =
      builder.setPlacement(placement)
      this

    /** Sets the state generator used for the population.
      *
      * @param generator
      *   state generator.
      * @return
      *   this population configuration.
      */
    private infix def generatedBy(generator: Int => S): PopulationConfig[S] =
      current = Some(generator)
      builder.setPopulation(size, generator)
      this

  /** Concrete mutable implementation of [[EnvironmentBuilder]].
    *
    * @tparam S
    *   state type associated with the agents.
    */
  private class EnvironmentBuilderImpl[S] extends EnvironmentBuilder[S]:

    private var world: Option[(Space, BoundaryPolicy)] = None
    private var perceptionRadius: Double = 10.0
    private var populationSize: Int = 0
    private var stateAt: Option[Int => S] = None
    private var positionAt: Option[Int => P2d] = None
    private var poisAcc: List[POI] = Nil
    private var memoryCapacity: Option[Int] = None

    /** Stores the configured space and boundary policy.
      *
      * @param space
      *   configured space.
      * @param boundaryPolicy
      *   configured boundary policy.
      */
    override def setSpace(space: Space, boundaryPolicy: BoundaryPolicy): Unit = world = Some((space, boundaryPolicy))

    /** Stores the configured perception radius.
      *
      * @param radius
      *   perception radius.
      */
    override def setPerceptionRadius(radius: Double): Unit = perceptionRadius = radius

    /** Stores the population size and state generator.
      *
      * @param size
      *   number of agents.
      * @param generator
      *   state generator.
      */
    override def setPopulation(size: Int, generator: Int => S): Unit =
      populationSize = size
      stateAt = Some(generator)

    /** Stores the position generator.
      *
      * @param placement
      *   position generator.
      */
    override def setPlacement(placement: Int => P2d): Unit = positionAt = Some(placement)

    /** Stores the memory capacity.
      *
      * @param capacity
      *   memory capacity.
      */
    override def setMemoryCapacity(capacity: Int): Unit = memoryCapacity = Some(capacity)

    /** Adds a point of interest to the accumulated list.
      *
      * @param poi
      *   point of interest to add.
      */
    override def addPoi(poi: POI): Unit = poisAcc = poi :: poisAcc

    /** Builds the environment specification after validating the required configuration values.
      *
      * @return
      *   the configured environment specification.
      * @throws IllegalArgumentException
      *   if the space or population has not been configured, or if the population size is not positive.
      */
    override def build(): EnvironmentSpec[S] = (world, stateAt) match
      case (Some((space, boundary)), Some(generator)) =>
        require(populationSize > 0, "Population size must be positive")
        EnvironmentSpec(
          space,
          boundary,
          perceptionRadius,
          populationSize,
          generator,
          positionAt.getOrElse(_ => space.randomPosition),
          poisAcc.reverse,
          memoryCapacity
        )
      case (None, _) => throw IllegalArgumentException("Cannot build an environment without a space")
      case (_, None) => throw IllegalArgumentException("Cannot build an environment without a population")
