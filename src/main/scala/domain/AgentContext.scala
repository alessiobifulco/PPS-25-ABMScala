package domain

case class AgentContext[S](focus: Agent[S], neighbors: List[Agent[S]], tick: Int)

object AgentContext:

  extension [S](ctx: AgentContext[S])

    def visibleWithin(radius: Double): List[Agent[S]] = ctx.neighbors
      .filter(n => (n.position - ctx.focus.position).length <= radius)

    def heardBeliefs: List[Belief] = ctx.neighbors.flatMap(_.remembers)
