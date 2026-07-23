package domain

trait Environment[S]:

  def bounds: Bounds

  def agents: List[Agent[S]]

  def withAgents(newAgents: List[Agent[S]]): Environment[S]

  def neighborsOf(agent: Agent[S], radius: Double)(using strategy: NeighborStrategy[S]): List[Agent[S]]

object Environment:

  def apply[S](bounds: Bounds, agents: List[Agent[S]]): Environment[S] = EnvironmentImpl(bounds, agents)

  private final case class EnvironmentImpl[S](bounds: Bounds, agents: List[Agent[S]]) extends Environment[S]:

    override def withAgents(newAgents: List[Agent[S]]): Environment[S] = copy(agents = newAgents)

    override def neighborsOf(agent: Agent[S], radius: Double)(using strategy: NeighborStrategy[S]): List[Agent[S]] =
      strategy.neighborsOf(agent, agents, radius)
