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

  def moveTowardsRemembered[S](speed: Double): ActionSource[S] = ctx =>
    rememberedPosition(ctx) match
      case Some(position) => moveTowardsPosition(position, speed)(ctx)
      case _              => List.empty

  def stopMoving[S]: ActionSource[S] = _ => List(Move(V2d.zero))

  def die[S]: ActionSource[S] = _ => List(Die())

  def spawn[S](state: S): ActionSource[S] = _ => List(Spawn(state))

  def tellNeighbours[S]: ActionSource[S] = ctx =>
    ctx.focus.memory.flatMap(_.latest) match
      case Some(belief) => ctx.neighbors.map(n => ShareMemory(n.id, belief.event))
      case _            => List.empty

  def learnFromNeighbours[S]: ActionSource[S] = ctx =>
    ctx.heardBeliefs.maxByOption(_.at) match
      case Some(belief) => List(Remember(belief.event))
      case _            => List.empty

  private def rememberedPosition[S](ctx: AgentContext[S]): Option[P2d] =
    ctx.focus.memory.flatMap(_.sightings.lastOption).map(_.event) match
      case Some(MemoryEvent.Sighting(_, position)) => Some(position)
      case _                                       => Option.empty
