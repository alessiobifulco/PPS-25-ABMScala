package domain

/** A function representing a predicate evaluated against an agent's current situation. It is typically used to
  * constrain the applicability of an [[InteractionRule]] or to guard the emission of [[Action]]s inside a [[Behavior]].
  */
type Condition[S] = AgentContext[S] => Boolean

/** Represents a snapshot of the agent's worldview at a specific point in time. It encapsulates the agent itself, its
  * surroundings, and the temporal context, providing all the necessary information to make decisions or evaluate rules.
  *
  * @param focus
  *   The central [[Agent]] to which this context belongs.
  * @param neighbors
  *   A list of other [[Agent]]s currently perceived by the focus agent.
  * @param tick
  *   The current discrete time step of the simulation.
  * @param residency
  *   How many consecutive ticks the agent has been standing inside each Point of Interest ([[POI]]).
  * @tparam S
  *   The generic type representing the internal state of the Agent.
  */
case class AgentContext[S](
    focus: Agent[S],
    neighbors: List[Agent[S]],
    tick: Int,
    residency: Residency = Residency.empty
)

object AgentContext:

  extension [S](ctx: AgentContext[S])

    /** Filters the perceived neighbors to include only those lying within a given physical distance.
      *
      * @param radius
      *   The maximum distance, inclusive, to consider an agent as visible.
      * @return
      *   A list of [[Agent]]s whose distance from the focus agent does not exceed the radius.
      */
    def visibleWithin(radius: Double): List[Agent[S]] = ctx.neighbors
      .filter(n => (n.position - ctx.focus.position).length <= radius)

    /** Collects all the beliefs currently held in the memories of the perceived neighbors. Useful for modeling
      * knowledge sharing and information diffusion among agents.
      *
      * @return
      *   A list of [[Belief]]s gathered from the neighbors.
      */
    def heardBeliefs: List[Belief] = ctx.neighbors.flatMap(_.remembers)

    /** Checks whether the focus agent's current position falls within the boundaries of a given Point of Interest.
      *
      * @param poi
      *   The Point of Interest ([[POI]]) to check against.
      * @return
      *   True if the agent is physically inside the POI, false otherwise.
      */
    def isInside(poi: POI): Boolean = poi.contains(ctx.focus.position)

    /** Evaluates whether the focus agent has remained inside a given Point of Interest for enough consecutive ticks to
      * trigger its activation (i.e., exceeding the POI's activation delay).
      *
      * @param poi
      *   The Point of Interest ([[POI]]) to evaluate.
      * @return
      *   True if the agent has settled in the POI, false otherwise.
      */
    def hasSettledIn(poi: POI): Boolean = ctx.residency.ticksIn(poi.id) > poi.activationDelay
