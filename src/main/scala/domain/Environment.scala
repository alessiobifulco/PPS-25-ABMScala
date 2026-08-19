package domain

trait Environment[S]:
  def space: Space
  def agents: List[Agent[S]]
  def boundaryPolicy: BoundaryPolicy
  def pois: List[POI]
  def withAgents(newAgents: List[Agent[S]]): Environment[S]
  def withPois(newPois: List[POI]): Environment[S]
  def neighborsOf(agent: Agent[S], radius: Double)(using strategy: NeighborStrategy[S]): List[Agent[S]]
  def neighborhoods(radius: Double)(using strategy: NeighborStrategy[S]): Agent[S] => List[Agent[S]]

object Environment:
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

  private final case class EnvironmentImpl[S](
      space: Space,
      agents: List[Agent[S]],
      boundaryPolicy: BoundaryPolicy,
      pois: List[POI]
  ) extends Environment[S]:

    override def withAgents(newAgents: List[Agent[S]]): Environment[S] = copy(agents = newAgents)

    override def withPois(newPois: List[POI]): Environment[S] = copy(pois = newPois)

    override def neighborsOf(agent: Agent[S], radius: Double)(using strategy: NeighborStrategy[S]): List[Agent[S]] =
      strategy.neighborsOf(agent, agents, radius)

    override def neighborhoods(radius: Double)(using strategy: NeighborStrategy[S]): Agent[S] => List[Agent[S]] =
      strategy.prepare(agents, radius)
