package domain

trait NeighborStrategy[S]:

  def prepare(all: List[Agent[S]], radius: Double): Agent[S] => List[Agent[S]]

  def neighborsOf(agent: Agent[S], all: List[Agent[S]], radius: Double): List[Agent[S]] = prepare(all, radius)(agent)

object NeighborStrategy:

  def bruteForce[S]: NeighborStrategy[S] = BruteForceStrategy[S]()

  def grid[S](cellSize: Double): NeighborStrategy[S] = GridStrategy[S](cellSize)

  def spatialHash[S](cellSize: Double): NeighborStrategy[S] = SpatialHashStrategy[S](cellSize)

  given defaultStrategy[S]: NeighborStrategy[S] = bruteForce[S]

  private def validateRadius(radius: Double): Unit =
    require(radius >= 0 && radius.isFinite, "Radius must be a finite non-negative number")

  private def validatedRadiusSquared(radius: Double): Double =
    validateRadius(radius)
    radius * radius

  private def squaredDistance(first: P2d, second: P2d): Double =
    val deltaX = first.x - second.x
    val deltaY = first.y - second.y
    deltaX * deltaX + deltaY * deltaY

  private final case class BruteForceStrategy[S]() extends NeighborStrategy[S]:

    override def prepare(all: List[Agent[S]], radius: Double): Agent[S] => List[Agent[S]] =
      val radiusSquared = validatedRadiusSquared(radius)
      agent =>
        all.filter { other => other.id != agent.id && squaredDistance(agent.position, other.position) <= radiusSquared }

  private final case class GridStrategy[S](cellSize: Double) extends NeighborStrategy[S]:

    require(cellSize > 0, "Cell size must be positive")

    override def prepare(all: List[Agent[S]], radius: Double): Agent[S] => List[Agent[S]] =
      val radiusSquared = validatedRadiusSquared(radius)
      val spatialIndex = buildIndex(all)
      val cellRange = math.ceil(radius / cellSize).toInt

      agent =>
        val focalCell = cellOf(agent.position)
        val candidateCells =
          for
            cellX <- focalCell._1 - cellRange to focalCell._1 + cellRange
            cellY <- focalCell._2 - cellRange to focalCell._2 + cellRange
          yield (cellX, cellY)

        candidateCells.flatMap(spatialIndex.getOrElse(_, Nil)).filter { other =>
          other.id != agent.id && squaredDistance(agent.position, other.position) <= radiusSquared
        }.toList

    private def cellOf(position: P2d): (Int, Int) =
      (math.floor(position.x / cellSize).toInt, math.floor(position.y / cellSize).toInt)

    private def buildIndex(agents: List[Agent[S]]): Map[(Int, Int), List[Agent[S]]] = agents
      .foldLeft(Map.empty[(Int, Int), List[Agent[S]]]) { (index, agent) =>
        val cell = cellOf(agent.position)
        val cellAgents = index.getOrElse(cell, Nil)
        index.updated(cell, cellAgents :+ agent)
      }

  private final case class SpatialHashStrategy[S](cellSize: Double) extends NeighborStrategy[S]:

    require(cellSize > 0, "Cell size must be positive")

    override def prepare(all: List[Agent[S]], radius: Double): Agent[S] => List[Agent[S]] =
      val radiusSquared = validatedRadiusSquared(radius)
      val spatialIndex = buildIndex(all)
      val cellRange = math.ceil(radius / cellSize).toInt

      agent =>
        val focalCell = cellOf(agent.position)
        val candidates =
          for
            cellX <- focalCell._1 - cellRange to focalCell._1 + cellRange
            cellY <- focalCell._2 - cellRange to focalCell._2 + cellRange
            other <- spatialIndex.getOrElse(hashCell(cellX, cellY), Nil) if other.id != agent.id
            if squaredDistance(agent.position, other.position) <= radiusSquared
          yield other
        candidates.toList

    private def cellOf(position: P2d): (Int, Int) =
      (math.floor(position.x / cellSize).toInt, math.floor(position.y / cellSize).toInt)

    private def hashCell(columnIndex: Int, rowIndex: Int): Long =
      (columnIndex.toLong << 32) ^ (rowIndex.toLong & 0xffffffffL)

    private def buildIndex(agents: List[Agent[S]]): Map[Long, List[Agent[S]]] = agents
      .foldLeft(Map.empty[Long, List[Agent[S]]]) { (index, agent) =>
        val cell = cellOf(agent.position)
        val hashKey = hashCell(cell._1, cell._2)
        val cellAgents = index.getOrElse(hashKey, Nil)
        index.updated(hashKey, cellAgents :+ agent)
      }
