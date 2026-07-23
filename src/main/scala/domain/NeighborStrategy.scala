package domain

trait NeighborStrategy[S]:

  def neighborsOf(agent: Agent[S], all: List[Agent[S]], radius: Double): List[Agent[S]]

private[domain] object NeighborDistance:

  def squared(first: P2d, second: P2d): Double =
    val deltaX = first.x - second.x
    val deltaY = first.y - second.y

    deltaX * deltaX + deltaY * deltaY

  private def validateRadius(radius: Double): Unit =
    require(radius >= 0 && radius.isFinite, "Radius must be a finite non-negative number")

  def validatedRadiusSquared(radius: Double): Double =
    validateRadius(radius)
    radius * radius

private[domain] object NeighborStrategySupport:

  private def cellOf(position: P2d, cellSize: Double): (Int, Int) =
    (math.floor(position.x / cellSize).toInt, math.floor(position.y / cellSize).toInt)

  private def buildIndex[S, K](
      agents: List[Agent[S]],
      cellSize: Double,
      keyOf: (Int, Int) => K
  ): Map[K, List[Agent[S]]] = agents.foldLeft(Map.empty[K, List[Agent[S]]]) { (index, agent) =>
    val cell = cellOf(agent.position, cellSize)
    val key = keyOf(cell._1, cell._2)
    val currentAgents = index.getOrElse(key, Nil)
    index.updated(key, currentAgents :+ agent)
  }

  def filterByDistance[S](agent: Agent[S], candidates: List[Agent[S]], radiusSquared: Double): List[Agent[S]] =
    candidates.filter { other =>
      other.id != agent.id && NeighborDistance.squared(agent.position, other.position) <= radiusSquared
    }

  def indexedNeighbors[S, K](
      agent: Agent[S],
      all: List[Agent[S]],
      radius: Double,
      cellSize: Double,
      keyOf: (Int, Int) => K
  ): List[Agent[S]] =
    val radiusSquared = NeighborDistance.validatedRadiusSquared(radius)
    val spatialIndex = buildIndex(all, cellSize, keyOf)
    val focalCell = cellOf(agent.position, cellSize)
    val cellRange = math.ceil(radius / cellSize).toInt

    val candidateCells =
      for
        cellX <- focalCell._1 - cellRange to focalCell._1 + cellRange
        cellY <- focalCell._2 - cellRange to focalCell._2 + cellRange
      yield (cellX, cellY)

    val candidates = candidateCells.toList.flatMap { case (cellX, cellY) =>
      spatialIndex.getOrElse(keyOf(cellX, cellY), Nil)
    }

    filterByDistance(agent, candidates, radiusSquared)

final class BruteForceNeighborStrategy[S] extends NeighborStrategy[S]:

  override def neighborsOf(agent: Agent[S], all: List[Agent[S]], radius: Double): List[Agent[S]] =
    val radiusSquared = NeighborDistance.validatedRadiusSquared(radius)
    NeighborStrategySupport.filterByDistance(agent, all, radiusSquared)

object NeighborStrategy:

  def bruteForce[S]: NeighborStrategy[S] = new BruteForceNeighborStrategy[S]

final class GridNeighborStrategy[S](val cellSize: Double) extends NeighborStrategy[S]:

  require(cellSize > 0, "Cell size must be positive")

  override def neighborsOf(agent: Agent[S], all: List[Agent[S]], radius: Double): List[Agent[S]] =
    NeighborStrategySupport
      .indexedNeighbors(agent = agent, all = all, radius = radius, cellSize = cellSize, keyOf = (x, y) => (x, y))

final class SpatialHashNeighborStrategy[S](val cellSize: Double) extends NeighborStrategy[S]:
  require(cellSize > 0, "Cell size must be positive")

  override def neighborsOf(agent: Agent[S], all: List[Agent[S]], radius: Double): List[Agent[S]] =
    NeighborStrategySupport
      .indexedNeighbors(agent = agent, all = all, radius = radius, cellSize = cellSize, keyOf = hashCell)

  private def hashCell(cellX: Int, cellY: Int): Long = (cellX.toLong << 32) ^ (cellY.toLong & 0xffffffffL)

final case class FilteredNeighborStrategy[S](
    base: NeighborStrategy[S],
    predicate: Agent[S] => Boolean,
    maximumResults: Option[Int] = None
) extends NeighborStrategy[S]:

  maximumResults.foreach { limit => require(limit >= 0, "Maximum results must be non-negative") }

  override def neighborsOf(agent: Agent[S], all: List[Agent[S]], radius: Double): List[Agent[S]] =
    val filteredNeighbors = base.neighborsOf(agent, all, radius).filter(predicate)
    maximumResults match
      case Some(limit) => filteredNeighbors.take(limit)
      case None        => filteredNeighbors
