package dsl

import domain.*
import domain.Action.*

/** Provides a rich set of declarative utilities, combinators, and primitive action generators designed to construct
  * agent behaviors fluidly within the DSL.
  */
object ConditionalBehavior:

  /** A functional alias representing a block of logic that produces a list of actions based on an agent's current
    * [[AgentContext]].
    */
  type ActionSource[S] = AgentContext[S] => List[Action[S]]

  private val directionChangeChance = 0.05

  /** Extension methods enabling a fluent, declarative algebra over [[ActionSource]] instances. It allows combining,
    * filtering, conditioning, and chaining actions together.
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
    * reversal is left to the [[BoundaryPolicy]] of the space.
    *
    * @param speed
    *   The movement magnitude.
    */
  def moveHorizontally[S](speed: Double): ActionSource[S] = ctx =>
    ctx.focus.velocity.x match
      case vx if vx < 0 => List(Move(V2d(-speed, 0)))
      case _            => List(Move(V2d(speed, 0)))

  /** Generates a movement action directed straight towards a specific absolute point.
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

  /** Generates a movement action towards the most recent POI sighting kept in memory, if any.
    *
    * @param speed
    *   The movement magnitude.
    */
  def moveTowardsRemembered[S](speed: Double): ActionSource[S] = ctx =>
    rememberedPosition(ctx) match
      case Some(position) => moveTowards(position, speed)(ctx)
      case _              => List.empty

  /** Generates a movement action away from the most recent POI sighting kept in memory, if any.
    *
    * @param speed
    *   The movement magnitude.
    */
  def moveAwayFromRemembered[S](speed: Double): ActionSource[S] = ctx =>
    rememberedPosition(ctx) match
      case Some(position) => moveAwayFrom(position, speed)(ctx)
      case _              => List.empty

  /** Generates a memory recording action whenever the agent steps inside any of the specified Points of Interest.
    */
  def rememberSightings[S](poiList: POI*): ActionSource[S] = ctx =>
    poiList.toList.filter(poi => ctx.isInside(poi)).map(poi => Remember(MemoryEvent.Sighting(poi.id, poi.position)))

  /** Generates an action to halt the movement, requesting a null velocity.
    */
  def stopMoving[S]: ActionSource[S] = _ => List(Move(V2d.zero))

  /** Generates a termination action.
    */
  def die[S]: ActionSource[S] = _ => List(Die())

  /** Generates a spawning action to create a new agent with the given state.
    */
  def spawn[S](state: S): ActionSource[S] = _ => List(Spawn(state))

  /** Generates communication actions to share the agent's latest belief with all perceived neighbors.
    */
  def tellNeighbours[S]: ActionSource[S] = ctx =>
    ctx.focus.memory.flatMap(_.latest) match
      case Some(belief) => ctx.neighbors.map(neighbor => Tell(neighbor.id, belief.event))
      case _            => List.empty

  /** Generates a memory-learning action by adopting the most recent belief heard from neighbors.
    */
  def learnFromNeighbours[S]: ActionSource[S] = ctx =>
    ctx.heardBeliefs.maxByOption(_.at) match
      case Some(belief) => List(Remember(belief.event))
      case _            => List.empty

  private def rememberedPosition[S](ctx: AgentContext[S]): Option[P2d] =
    ctx.focus.memory.flatMap(_.sightings.lastOption).map(_.event) match
      case Some(MemoryEvent.Sighting(_, position)) => Some(position)
      case _                                       => Option.empty
