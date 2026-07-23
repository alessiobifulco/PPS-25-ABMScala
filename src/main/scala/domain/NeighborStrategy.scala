package domain

trait NeighborStrategy[S]:

  def neighborsOf(agent: Agent[S], all: List[Agent[S]], radius: Double): List[Agent[S]]

final class BruteForceNeighborStrategy[S] extends NeighborStrategy[S]:

  override def neighborsOf(agent: Agent[S], all: List[Agent[S]], radius: Double): List[Agent[S]] =

    require(radius >= 0, "Radius must be non-negative")
    val radiusSquared = radius * radius

    all.filter { other => other.id != agent.id && distanceSquared(agent.position, other.position) <= radiusSquared }

  private def distanceSquared(first: P2d, second: P2d): Double =
    val deltaX = first.x - second.x
    val deltaY = first.y - second.y
    deltaX * deltaX + deltaY * deltaY

object NeighborStrategy:

  def bruteForce[S]: NeighborStrategy[S] = new BruteForceNeighborStrategy[S]
