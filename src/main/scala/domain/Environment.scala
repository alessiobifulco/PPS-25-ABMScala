package domain

trait Environment[S]:

  def bounds: Bounds

  def agents: List[Agent[S]]

  def withAgents(newAgents: List[Agent[S]]): Environment[S]

  def neighborsOf(agent: Agent[S], radius: Double): List[Agent[S]]


object Environment:

  def apply[S](bounds: Bounds, agents: List[Agent[S]]): Environment[S] =
    EnvironmentImpl(bounds, agents)

  private final case class EnvironmentImpl[S](bounds: Bounds, agents: List[Agent[S]]) extends Environment[S]:

    override def withAgents(newAgents: List[Agent[S]]): Environment[S] =
      copy(agents = newAgents)

    override def neighborsOf(agent: Agent[S], radius: Double): List[Agent[S]] =

      require(radius >= 0, "Radius must be non-negative")
      val radiusSquared = radius * radius

      agents.filter { other =>
        other.id != agent.id &&
          distanceSquared(agent.position, other.position) <= radiusSquared
      }

    private def distanceSquared(first: P2d, second: P2d): Double =
      val deltaX = first.x - second.x
      val deltaY = first.y - second.y
      deltaX * deltaX + deltaY * deltaY