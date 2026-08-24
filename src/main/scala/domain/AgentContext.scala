package domain

type Condition[S] = AgentContext[S] => Boolean

case class AgentContext[S](
    focus: Agent[S],
    neighbors: List[Agent[S]],
    tick: Int,
    residency: Residency = Residency.empty
)

object AgentContext:

  extension [S](ctx: AgentContext[S])

    def visibleWithin(radius: Double): List[Agent[S]] = ctx.neighbors
      .filter(n => (n.position - ctx.focus.position).length <= radius)

    def heardBeliefs: List[Belief] = ctx.neighbors.flatMap(_.remembers)

    def isInside(poi: POI): Boolean = poi.contains(ctx.focus.position)

    def hasSettledIn(poi: POI): Boolean = ctx.residency.ticksIn(poi.id) > poi.activationDelay
