package domain

trait NeighborStrategy[S]:

  def prepare(all: List[Agent[S]], radius: Double): Agent[S] => List[Agent[S]]

  def neighborsOf(agent: Agent[S], all: List[Agent[S]], radius: Double): List[Agent[S]] = prepare(all, radius)(agent)

object NeighborStrategy:

  def bruteForce[S]: NeighborStrategy[S] = BruteForceStrategy[S]()

  def grid[S](cellSize: Double): NeighborStrategy[S] = GridStrategy[S](cellSize)

  given defaultStrategy[S]: NeighborStrategy[S] = bruteForce[S]

  private def validatedRadiusSquared(radius: Double): Double =
    require(radius >= 0 && radius.isFinite, "Radius must be a finite non-negative number")
    radius * radius

  private def squaredDistance(first: P2d, second: P2d): Double =
    val deltaX = first.x - second.x
    val deltaY = first.y - second.y
    deltaX * deltaX + deltaY * deltaY

  private final case class BruteForceStrategy[S]() extends NeighborStrategy[S]:

    override def prepare(all: List[Agent[S]], radius: Double): Agent[S] => List[Agent[S]] =
      val radiusSquared = validatedRadiusSquared(radius)
      agent =>
        all.filter: other =>
          other.id != agent.id && squaredDistance(agent.position, other.position) <= radiusSquared

  private final case class GridStrategy[S](cellSize: Double) extends NeighborStrategy[S]:

    require(cellSize > 0, "Cell size must be positive")

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

    private def cellOf(position: P2d): (Int, Int) =
      (math.floor(position.x / cellSize).toInt, math.floor(position.y / cellSize).toInt)

    private def buildIndex(agents: List[Agent[S]]): Map[(Int, Int), List[Agent[S]]] = agents
      .foldLeft(Map.empty[(Int, Int), List[Agent[S]]]): (index, agent) =>
        val cell = cellOf(agent.position)
        index.updated(cell, agent :: index.getOrElse(cell, Nil))
