package dsl

import domain.*

object ConditionalBehaviour:

  type ActionSource[S] = AgentContext[S] => List[Action[S]]

  private val directionChangeChance = 0.05

  extension [S](source: ActionSource[S])

    infix def whenAgentIs(state: S)(using builder: ChoicesBuilder[S]): Unit = builder
      .addChoice(Choice(ctx => ctx.focus.state == state, source))

    infix def to(other: ActionSource[S]): ActionSource[S] = ctx => source(ctx) ++ other(ctx)

    infix def orElse(other: ActionSource[S]): ActionSource[S] = ctx =>
      source(ctx) match
        case Nil     => other(ctx)
        case actions => actions

    infix def onlyIf(condition: AgentContext[S] => Boolean): ActionSource[S] =
      ctx => if condition(ctx) then source(ctx) else List.empty

    infix def vanishingWith(c: Chance): ActionSource[S] = ctx => if c.happens then source(ctx) :+ Die() else source(ctx)

  def asDefault[S](source: ActionSource[S])(using builder: ChoicesBuilder[S]): Unit = builder.setDefault(source)

  def moveRandomly[S](speed: Double): ActionSource[S] = ctx =>
    ctx.focus.velocity match
      case v if v.length > 0 && math.random() > directionChangeChance => List(Move(v.normalized * speed))
      case _                                                          => List(Move(V2d.random() * speed))

  def moveHorizontally[S](speed: Double): ActionSource[S] = ctx =>
    ctx.focus.velocity.x match
      case vx if vx < 0 => List(Move(V2d(-speed, 0)))
      case _            => List(Move(V2d(speed, 0)))

  private def moveTowardsPosition[S](target: P2d, speed: Double): ActionSource[S] =
    ctx => List(Move((target - ctx.focus.position).normalized * speed))

  def moveTowardsPoi[S](poi: POI, speed: Double): ActionSource[S] = moveTowardsPosition(poi.position, speed)

  def rememberSightings[S](poiList: POI*): ActionSource[S] = ctx =>
    poiList.toList.filter(poi => ctx.isInside(poi)).map(poi => Remember(MemoryEvent.Sighting(poi.id, poi.position)))

  def rememberEncounters[S](goesWell: S => Boolean): ActionSource[S] = ctx =>
    nearest(ctx, ctx.neighbors) match
      case Some(other) => List(Remember(MemoryEvent.Encounter(other.id, goesWell(other.state))))
      case _           => List.empty

  def moveTowardsRemembered[S](speed: Double): ActionSource[S] = ctx =>
    rememberedPosition(ctx) match
      case Some(position) => moveTowardsPosition(position, speed)(ctx)
      case _              => List.empty

  def avoidRemembered[S](speed: Double, about: MemoryEvent => Boolean): ActionSource[S] = ctx =>
    nearest(ctx, ctx.neighbors.filter(other => rememberedAgents(ctx, about).contains(other.id))) match
      case Some(other) => List(Move((ctx.focus.position - other.position).normalized * speed))
      case _           => List.empty

  def stopMoving[S]: ActionSource[S] = _ => List(Move(V2d.zero))

  def die[S]: ActionSource[S] = _ => List(Die())

  def forget[S]: ActionSource[S] = _ => List(Forget())

  def spawn[S](state: S): ActionSource[S] = _ => List(Spawn(state))

  def tellNeighbours[S]: ActionSource[S] = ctx =>
    ctx.focus.memory.flatMap(_.latest) match
      case Some(belief) => ctx.neighbors.map(n => ShareMemory(n.id, belief.event))
      case _            => List.empty

  def learnFromNeighbours[S](about: MemoryEvent => Boolean = _ => true): ActionSource[S] = ctx =>
    ctx.heardBeliefs.filter(belief => about(belief.event)).maxByOption(_.at) match
      case Some(belief) => List(Remember(belief.event))
      case _            => List.empty

  private def nearest[S](ctx: AgentContext[S], among: List[Agent[S]]): Option[Agent[S]] = among
    .minByOption(other => (other.position - ctx.focus.position).length)

  private def rememberedAgents[S](ctx: AgentContext[S], about: MemoryEvent => Boolean): List[AgentId] = ctx.focus
    .remembers.map(_.event).filter(about).collect:
      case MemoryEvent.Encounter(other, _) => other

  private def rememberedPosition[S](ctx: AgentContext[S]): Option[P2d] =
    ctx.focus.memory.flatMap(_.sightings.lastOption).map(_.event) match
      case Some(MemoryEvent.Sighting(_, position)) => Some(position)
      case _                                       => Option.empty
