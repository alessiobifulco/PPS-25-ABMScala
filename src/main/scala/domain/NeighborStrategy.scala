package domain

/** Strategy used to calculate the neighbours of agents.
  *
  * @tparam S
  *   state type associated with the agents.
  */
trait NeighborStrategy[S]:

  /** Prepares a function that retrieves the neighbours of a specific agent.
    *
    * @param all
    *   all the agents that can be considered in the search.
    * @param radius
    *   maximum distance used for the search.
    * @return
    *   a function that associates each agent with its neighbours.
    */
  def prepare(all: List[Agent[S]], radius: Double): Agent[S] => List[Agent[S]]

  /** Retrieves the neighbours of an agent.
    *
    * @param agent
    *   agent for which the neighbours must be searched.
    * @param all
    *   all the agents that can be considered in the search.
    * @param radius
    *   maximum distance used for the search.
    * @return
    *   a list containing the neighbouring agents.
    */
  def neighborsOf(agent: Agent[S], all: List[Agent[S]], radius: Double): List[Agent[S]] = prepare(all, radius)(agent)

/** Factory methods and default values for [[NeighborStrategy]].
  */
object NeighborStrategy:

  /** Creates a strategy that checks every agent when searching for neighbours.
    *
    * @tparam S
    *   state type associated with the agents.
    * @return
    *   a brute-force neighbour strategy.
    */
  def bruteForce[S]: NeighborStrategy[S] = BruteForceStrategy[S]()

  /** Creates a strategy that uses a spatial grid to search for neighbours.
    *
    * @param cellSize
    *   size of each cell in the spatial grid.
    * @tparam S
    *   state type associated with the agents.
    * @return
    *   a grid-based neighbour strategy.
    */
  def grid[S](cellSize: Double): NeighborStrategy[S] = GridStrategy[S](cellSize)

  /** Default neighbour strategy based on a brute-force search.
    *
    * @tparam S
    *   state type associated with the agents.
    */
  given defaultStrategy[S]: NeighborStrategy[S] = bruteForce[S]

  /** Validates a radius and returns its squared value.
    *
    * @param radius
    *   radius to validate.
    * @return
    *   the squared radius.
    * @throws IllegalArgumentException
    *   if the radius is negative or not finite.
    */
  private def validatedRadiusSquared(radius: Double): Double =
    require(radius >= 0 && radius.isFinite, "Radius must be a finite non-negative number")
    radius * radius

  /** Calculates the squared Euclidean distance between two positions.
    *
    * @param first
    *   first position.
    * @param second
    *   second position.
    * @return
    *   the squared distance between the two positions.
    */
  private def squaredDistance(first: P2d, second: P2d): Double =
    val deltaX = first.x - second.x
    val deltaY = first.y - second.y
    deltaX * deltaX + deltaY * deltaY

  /** Neighbour strategy that checks all the available agents for every search.
    *
    * @tparam S
    *   state type associated with the agents.
    */
  private final case class BruteForceStrategy[S]() extends NeighborStrategy[S]:

    /** Prepares a function that searches neighbours by checking every agent.
      *
      * @param all
      *   all the agents that can be considered in the search.
      * @param radius
      *   maximum distance used for the search.
      * @return
      *   a function that associates each agent with its neighbours.
      */
    override def prepare(all: List[Agent[S]], radius: Double): Agent[S] => List[Agent[S]] =
      val radiusSquared = validatedRadiusSquared(radius)
      agent =>
        all.filter: other =>
          other.id != agent.id && squaredDistance(agent.position, other.position) <= radiusSquared

  /** Neighbour strategy that uses a spatial grid to reduce the number of agents considered during a search.
    *
    * @param cellSize
    *   size of each cell in the spatial grid.
    * @tparam S
    *   state type associated with the agents.
    */
  private final case class GridStrategy[S](cellSize: Double) extends NeighborStrategy[S]:

    require(cellSize > 0, "Cell size must be positive")

    /** Prepares a function that searches neighbours using a spatial index.
      *
      * @param all
      *   all the agents that can be considered in the search.
      * @param radius
      *   maximum distance used for the search.
      * @return
      *   a function that associates each agent with its neighbours.
      */
    override def prepare(all: List[Agent[S]], radius: Double): Agent[S] => List[Agent[S]] =
      val radiusSquared = validatedRadiusSquared(radius)
      val spatialIndex = buildIndex(all)
      val cellRange = math.ceil(radius / cellSize).toInt

      agent =>
        val (focalX, focalY) = cellOf(agent.position)
        val candidates =
          for
            cellX <- focalX - cellRange to focalX + cellRange
            cellY <- focalY - cellRange to focalY + cellRange
            other <- spatialIndex.getOrElse((cellX, cellY), Nil)
            if other.id != agent.id && squaredDistance(agent.position, other.position) <= radiusSquared
          yield other
        candidates.toList

    /** Calculates the coordinates of the grid cell containing a position.
      *
      * @param position
      *   position for which the grid cell must be calculated.
      * @return
      *   the coordinates of the corresponding grid cell.
      */
    private def cellOf(position: P2d): (Int, Int) =
      (math.floor(position.x / cellSize).toInt, math.floor(position.y / cellSize).toInt)

    /** Builds a spatial index by associating every agent with its grid cell.
      *
      * @param agents
      *   agents to insert into the spatial index.
      * @return
      *   a map containing the agents grouped by grid cell.
      */
    private def buildIndex(agents: List[Agent[S]]): Map[(Int, Int), List[Agent[S]]] = agents
      .foldLeft(Map.empty[(Int, Int), List[Agent[S]]]): (index, agent) =>
        val cell = cellOf(agent.position)
        index.updated(cell, agent :: index.getOrElse(cell, Nil))
