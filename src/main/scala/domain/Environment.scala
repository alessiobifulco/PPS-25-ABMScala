package domain

trait Environment[S]:
  def space: Space
  def agents: List[Agent[S]]
  def boundaryPolicy: BoundaryPolicy
  def withAgents(newAgents: List[Agent[S]]): Environment[S]
  def neighborsOf(agent: Agent[S], radius: Double)(using strategy: NeighborStrategy[S]): List[Agent[S]]

object Environment:
  def apply[S](space: Space, agents: List[Agent[S]], boundaryPolicy: BoundaryPolicy = BouncePolicy): Environment[S] =
    require(boundaryPolicy != WrapPolicy || space.isInstanceOf[Toroidal], "WrapPolicy requires a toroidal space")
    EnvironmentImpl(space, agents, boundaryPolicy)

  private final case class EnvironmentImpl[S](space: Space, agents: List[Agent[S]], boundaryPolicy: BoundaryPolicy)
      extends Environment[S]:
    override def withAgents(newAgents: List[Agent[S]]): Environment[S] = copy(agents = newAgents)
    override def neighborsOf(agent: Agent[S], radius: Double)(using strategy: NeighborStrategy[S]): List[Agent[S]] =
      strategy.neighborsOf(agent, agents, radius)
