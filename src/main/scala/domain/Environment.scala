package domain

/** Environment containing the space, the agents and the points of interest used in a simulation.
  *
  * @tparam S
  *   state type associated with the agents.
  */
trait Environment[S]:

  /** Space in which the simulation takes place.
    */
  def space: Space

  /** Agents currently contained in the environment.
    */
  def agents: List[Agent[S]]

  /** Boundary policy applied to agents reaching the space boundary.
    */
  def boundaryPolicy: BoundaryPolicy

  /** Points of interest contained in the environment.
    */
  def pois: List[POI]

  /** Creates a new environment with the specified agents.
    *
    * @param newAgents
    *   agents to use in the new environment.
    * @return
    *   a new environment containing the input agents.
    */
  def withAgents(newAgents: List[Agent[S]]): Environment[S]

  /** Creates a new environment with the specified points of interest.
    *
    * @param newPois
    *   points of interest to use in the new environment.
    * @return
    *   a new environment containing the input points of interest.
    */
  def withPois(newPois: List[POI]): Environment[S]

  /** Retrieves the agents located within the specified radius of an agent.
    *
    * @param agent
    *   agent for which the neighbours must be searched.
    * @param radius
    *   maximum distance used for the search.
    * @param strategy
    *   strategy used to calculate the neighbours.
    * @return
    *   a list containing the neighbouring agents.
    */
  def neighborsOf(agent: Agent[S], radius: Double)(using strategy: NeighborStrategy[S]): List[Agent[S]]

  /** Creates a function that retrieves the neighbours of an agent using the specified radius.
    *
    * @param radius
    *   maximum distance used for the search.
    * @param strategy
    *   strategy used to calculate the neighbourhoods.
    * @return
    *   a function that associates each agent with its neighbouring agents.
    */
  def neighborhoods(radius: Double)(using strategy: NeighborStrategy[S]): Agent[S] => List[Agent[S]]

/** Factory methods for creating an [[Environment]].
  */
object Environment:

  /** Creates a new environment after validating its boundary policy and points of interest.
    *
    * @param space
    *   space in which the simulation takes place.
    * @param agents
    *   agents contained in the environment.
    * @param boundaryPolicy
    *   policy applied when an agent reaches the space boundary.
    * @param pois
    *   points of interest contained in the environment.
    * @tparam S
    *   state type associated with the agents.
    * @return
    *   a new environment initialized with the specified values.
    * @throws IllegalArgumentException
    *   if the wrap policy is used with a non-toroidal space or a point of interest is outside the space.
    */
  def apply[S](
      space: Space,
      agents: List[Agent[S]],
      boundaryPolicy: BoundaryPolicy = BoundaryPolicy.bounce,
      pois: List[POI] = Nil
  ): Environment[S] =
    require(
      boundaryPolicy != BoundaryPolicy.wrap || space.isInstanceOf[Toroidal],
      "WrapPolicy requires a toroidal space"
    )
    require(pois.forall(poi => space.contains(poi.position)), "Every POI must lie inside the space")
    EnvironmentImpl(space, agents, boundaryPolicy, pois)

  /** Private implementation of an [[Environment]].
    *
    * @param space
    *   space in which the simulation takes place.
    * @param agents
    *   agents contained in the environment.
    * @param boundaryPolicy
    *   policy applied when an agent reaches the space boundary.
    * @param pois
    *   points of interest contained in the environment.
    * @tparam S
    *   state type associated with the agents.
    */
  private final case class EnvironmentImpl[S](
      space: Space,
      agents: List[Agent[S]],
      boundaryPolicy: BoundaryPolicy,
      pois: List[POI]
  ) extends Environment[S]:

    /** Creates a new environment with the specified agents.
      *
      * @param newAgents
      *   agents to use in the new environment.
      * @return
      *   a new environment containing the input agents.
      */
    override def withAgents(newAgents: List[Agent[S]]): Environment[S] = copy(agents = newAgents)

    /** Creates a new environment with the specified points of interest.
      *
      * @param newPois
      *   points of interest to use in the new environment.
      * @return
      *   a new environment containing the input points of interest.
      */
    override def withPois(newPois: List[POI]): Environment[S] = copy(pois = newPois)

    /** Retrieves the agents located within the specified radius of an agent.
      *
      * @param agent
      *   agent for which the neighbours must be searched.
      * @param radius
      *   maximum distance used for the search.
      * @param strategy
      *   strategy used to calculate the neighbours.
      * @return
      *   a list containing the neighbouring agents.
      */
    override def neighborsOf(agent: Agent[S], radius: Double)(using strategy: NeighborStrategy[S]): List[Agent[S]] =
      strategy.neighborsOf(agent, agents, radius)

    /** Creates a function that retrieves the neighbours of an agent using the specified radius.
      *
      * @param radius
      *   maximum distance used for the search.
      * @param strategy
      *   strategy used to calculate the neighbourhoods.
      * @return
      *   a function that associates each agent with its neighbouring agents.
      */
    override def neighborhoods(radius: Double)(using strategy: NeighborStrategy[S]): Agent[S] => List[Agent[S]] =
      strategy.prepare(agents, radius)
