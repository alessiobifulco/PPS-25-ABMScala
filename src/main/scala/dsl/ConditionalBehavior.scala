package dsl

import domain.*
import domain.Action.*

/** The vocabulary with which behaviors are written in the DSL: primitive action sources, the combinators that compose
  * them, and the operations that register them into the [[BehaviorsBuilder]].
  */
object ConditionalBehavior:

  /** A functional alias representing a block of logic that produces a list of actions based on an agent's current
    * [[AgentContext]].
    */
  type ActionSource[S] = AgentContext[S] => List[Action[S]]

  private val directionChangeChance = 0.05

  /** Combinators over [[ActionSource]]. Each of them takes action sources and returns an action source, so that the
    * result of a combination can be combined again.
    */
  extension [S](source: ActionSource[S])

    /** Registers this action source as a state-specific behavior into the implicit [[BehaviorsBuilder]].
      *
      * @param state
      *   The target state required to trigger this behavior.
      * @param builder
      *   The implicit parent builder.
      */
    infix def whenAgentIs(state: S)(using builder: BehaviorsBuilder[S]): Unit = builder
      .add(Behavior(Some(state))(source))

    /** Combines this action source with another, concatenating in a single list the actions produced by both.
      *
      * @param other
      *   The additional action source to append.
      * @return
      *   A new combined [[ActionSource]].
      */
    infix def to(other: ActionSource[S]): ActionSource[S] = ctx => source(ctx) ++ other(ctx)

    /** Fallback combinator: if this action source produces no actions, it delegates execution to the alternative
      * source.
      *
      * @param other
      *   The fallback [[ActionSource]] to use if the primary one is empty.
      * @return
      *   The resulting [[ActionSource]].
      */
    infix def orElse(other: ActionSource[S]): ActionSource[S] = ctx =>
      source(ctx) match
        case Nil     => other(ctx)
        case actions => actions

    /** Guards the execution of this action source with a specific runtime [[Condition]].
      *
      * @param condition
      *   The predicate that must be satisfied.
      * @return
      *   A guarded [[ActionSource]], yielding no action at all whenever the condition is not met.
      */
    infix def onlyIf(condition: Condition[S]): ActionSource[S] =
      ctx => if condition(ctx) then source(ctx) else List.empty

    /** Stochastically attaches a self-termination ([[Die]]) action to the output based on a given probability.
      *
      * @param c
      *   The probability threshold ([[Chance]]).
      * @return
      *   The modified [[ActionSource]].
      */
    infix def vanishingWith(c: Chance): ActionSource[S] = ctx => if c.happens then source(ctx) :+ Die() else source(ctx)

  /** Registers this action source as a universal default behavior (state-independent) into the builder.
    *
    * @param source
    *   The default [[ActionSource]].
    * @param builder
    *   The implicit parent builder.
    */
  def asDefault[S](source: ActionSource[S])(using builder: BehaviorsBuilder[S]): Unit = builder
    .add(Behavior(Option.empty[S])(source))

  /** Generates a wandering movement: the agent keeps its current heading, picking a brand new random direction with a
    * small probability at every tick, or whenever it is currently still.
    *
    * @param speed
    *   The movement magnitude.
    */
  def moveRandomly[S](speed: Double): ActionSource[S] = ctx =>
    ctx.focus.velocity match
      case v if v.length > 0 && math.random() > directionChangeChance => List(Move(v.normalized * speed))
      case _                                                          => List(Move(V2d.random() * speed))

  /** Generates a horizontal-only movement, preserving the direction the agent is already heading to, so that the
    * reversal is left to the [[BoundaryPolicy]] of the space. An agent with no horizontal velocity starts moving right.
    *
    * @param speed
    *   The movement magnitude.
    */
  def moveHorizontally[S](speed: Double): ActionSource[S] = ctx =>
    ctx.focus.velocity.x match
      case vx if vx < 0 => List(Move(V2d(-speed, 0)))
      case _            => List(Move(V2d(speed, 0)))

  /** Generates a movement action directed straight towards a specific absolute point. An agent already standing on the
    * target stops, since normalizing the zero vector yields the zero vector.
    *
    * @param target
    *   The destination coordinate ([[P2d]]).
    * @param speed
    *   The movement magnitude.
    */
  def moveTowards[S](target: P2d, speed: Double): ActionSource[S] =
    ctx => List(Move((target - ctx.focus.position).normalized * speed))

  /** Generates a movement action directed away from a specific target point.
    *
    * @param target
    *   The coordinate to flee from ([[P2d]]).
    * @param speed
    *   The movement magnitude.
    */
  def moveAwayFrom[S](target: P2d, speed: Double): ActionSource[S] =
    ctx => List(Move((ctx.focus.position - target).normalized * speed))

  /** Generates a movement action towards the most recent POI sighting kept in memory. An agent with no memory, or with
    * no sighting recorded yet, produces no action.
    *
    * @param speed
    *   The movement magnitude.
    */
  def moveTowardsRemembered[S](speed: Double): ActionSource[S] =
    ctx => rememberedPosition(ctx).toList.flatMap(moveTowards(_, speed)(ctx))

  /** Generates a movement action away from the most recent POI sighting kept in memory. An agent with no memory, or
    * with no sighting recorded yet, produces no action.
    *
    * @param speed
    *   The movement magnitude.
    */
  def moveAwayFromRemembered[S](speed: Double): ActionSource[S] =
    ctx => rememberedPosition(ctx).toList.flatMap(moveAwayFrom(_, speed)(ctx))

  /** Generates a memory recording action for every specified Point of Interest currently containing the agent. The
    * action is produced at every tick the agent spends inside the POI, not only when it enters.
    */
  def rememberSightings[S](poiList: POI*): ActionSource[S] =
    ctx => for poi <- poiList.toList if ctx.isInside(poi) yield Remember(MemoryEvent.Sighting(poi.id, poi.position))

  /** Generates an action to halt the movement, requesting a zero velocity.
    */
  def stopMoving[S]: ActionSource[S] = _ => List(Move(V2d.zero))

  /** Generates a termination action.
    */
  def die[S]: ActionSource[S] = _ => List(Die())

  /** Generates a spawning action to create a new agent with the given state.
    */
  def spawn[S](state: S): ActionSource[S] = _ => List(Spawn(state))

  /** Generates communication actions to share the agent's latest belief with every perceived neighbor. An agent with no
    * belief, or with no neighbors, produces no action.
    */
  def tellNeighbours[S]: ActionSource[S] = ctx =>
    for
      belief <- ctx.focus.memory.flatMap(_.latest).toList
      neighbor <- ctx.neighbors
    yield Tell(neighbor.id, belief.event)

  /** Generates a memory-learning action by adopting the most recent belief heard from neighbors. An agent whose
    * neighbors remember nothing produces no action.
    */
  def learnFromNeighbours[S]: ActionSource[S] =
    ctx => for belief <- ctx.heardBeliefs.maxByOption(_.at).toList yield Remember(belief.event)

  /** The position of the most recent sighting kept in memory. It is missing when the agent has no memory or has not
    * sighted any POI yet; the last step is needed because a belief carries a generic [[MemoryEvent]], and only a
    * [[MemoryEvent.Sighting]] has a position.
    */
  private def rememberedPosition[S](ctx: AgentContext[S]): Option[P2d] =
    for
      memory <- ctx.focus.memory
      belief <- memory.sightings.lastOption
      position <- positionOf(belief.event)
    yield position

  private def positionOf(event: MemoryEvent): Option[P2d] = event match
    case MemoryEvent.Sighting(_, position) => Some(position)
    case _                                 => Option.empty
