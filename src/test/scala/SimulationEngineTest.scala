import domain.*
import domain.Action.*
import engine.*
import org.mockito.ArgumentMatchers.{any, anyDouble, anyInt}
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Integration suite of [[SimulationEngine]]. Each case runs one or more ticks on real behaviors, rules, space and
  * boundary policy, and the cases are grouped by the phases described in [[SimulationEngine.tick]].
  *
  * Two dependencies are replaced by Mockito mocks, stubbed where the engine needs an answer and verified afterwards:
  *   - [[NeighborStrategy]] returns a fixed neighborhood, so that perception does not depend on the search algorithm,
  *     and it is checked to be prepared once per tick with the population and the configured radius;
  *   - [[Memory]] returns itself on every update and records the calls it receives, so that the assertions concern
  *     which event the engine stores, with which tick and on which agent, rather than the retention policy of the real
  *     memory.
  *
  * The state type is `String`, since no case relies on properties of the state other than equality.
  */
class SimulationEngineTest extends AnyFlatSpec with Matchers:

  private val space = RectangularSpace(100.0, 100.0)
  private val radius = 20.0
  private val event = MemoryEvent.Encounter(AgentId(1), true)

  private def agentAt(id: Int, x: Double, state: String = "healthy", velocity: V2d = V2d.zero): Agent[String] =
    Agent(AgentId(id), P2d(x, 10.0), velocity, state)

  private def withMemory(id: Int, x: Double, memory: Memory): Agent[String] =
    Agent(AgentId(id), P2d(x, 10.0), V2d.zero, "healthy", Some(memory))

  private def configWith(
      agents: List[Agent[String]],
      behaviors: List[Behavior[String]] = List.empty,
      rules: List[InteractionRule[String]] = List.empty,
      poiList: List[POI] = List.empty,
      strategy: NeighborStrategy[String] = NeighborStrategy.bruteForce[String]
  ): SimulationConfig[String] =
    SimulationConfig(Environment(space, agents, BoundaryPolicy.bounce, poiList), behaviors, radius, rules, strategy)

  private def steps(config: SimulationConfig[String], times: Int = 1): SimulationState[String] = (1 to times)
    .foldLeft(SimulationEngine.init(config))((state, _) => SimulationEngine.tick(state, config))

  private def always(actions: Action[String]*): Behavior[String] = Behavior(Option.empty[String])(_ => actions.toList)

  private def onlyWhen(state: String)(actions: Action[String]*): Behavior[String] =
    Behavior(Some(state))(_ => actions.toList)

  private def rule(from: Option[String], applies: Boolean, to: String): InteractionRule[String] =
    InteractionRule(from, (_: AgentContext[String]) => applies)(_ => to)

  /** A memory mock answering every update with itself. Left unstubbed, `remember` would return null, which the engine
    * would store as the agent's memory and call again at the following tick.
    */
  private def memoryMock(): Memory =
    val memory = mock(classOf[Memory])
    when(memory.remember(anyInt(), any(classOf[MemoryEvent]))).thenReturn(memory)
    memory

  "The engine" should "start at tick zero, numbering newborns after the highest identifier in use" in:
    val agents = List(agentAt(0, 10.0), agentAt(5, 20.0))
    val state = SimulationEngine.init(configWith(agents))
    state.tick shouldBe 0
    state.environment.agents shouldBe agents
    state.nextId shouldBe 6

  it should "advance the tick by one without modifying the state it receives" in:
    val config = configWith(List(agentAt(0, 10.0)), behaviors = List(always(Move(V2d(1.0, 0.0)))))
    val start = SimulationEngine.init(config)
    val next = SimulationEngine.tick(start, config)
    next.tick shouldBe 1
    next.environment.agents.head.position shouldBe P2d(11.0, 10.0)
    start.tick shouldBe 0
    start.environment.agents.head.position shouldBe P2d(10.0, 10.0)

  "The perception phase" should "prepare the neighbor search once per tick and offer its result to every agent" in:
    val agents = List(agentAt(0, 10.0), agentAt(1, 50.0), agentAt(2, 90.0))
    val strategy = mock(classOf[NeighborStrategy[String]])
    val neighborhood = (_: Agent[String]) => List(agents.last)
    when(strategy.prepare(any(classOf[List[Agent[String]]]), anyDouble())).thenReturn(neighborhood)
    val counting = Behavior(Option.empty[String])(ctx => List(Move(V2d(0.0, ctx.neighbors.size.toDouble))))
    val moved = steps(configWith(agents, behaviors = List(counting), strategy = strategy)).environment.agents
    moved.map(_.position.y) shouldBe List(11.0, 11.0, 11.0)
    verify(strategy).prepare(agents, radius)
    verifyNoMoreInteractions(strategy)

  "The decision phase" should "fire only the first applicable behavior, skipping those bound to another state" in:
    val behaviors =
      List(onlyWhen("infected")(Move(V2d(1.0, 0.0))), always(Move(V2d(0.0, 1.0))), always(Move(V2d(5.0, 5.0))))
    steps(configWith(List(agentAt(0, 10.0)), behaviors = behaviors)).environment.agents.head.position shouldBe
      P2d(10.0, 11.0)

  it should "fire only the first applicable rule, skipping those whose condition does not hold" in:
    val rules = List(
      rule(Some("healthy"), applies = false, to = "infected"),
      rule(None, applies = true, to = "immune"),
      rule(Some("healthy"), applies = true, to = "dead")
    )
    steps(configWith(List(agentAt(0, 10.0)), rules = rules)).environment.agents.head.state shouldBe "immune"

  "The movement resolution" should "sum the requested velocities, or keep the current one when none is requested" in:
    val mover = agentAt(0, 10.0)
    val drifter = agentAt(1, 50.0, state = "drifting", velocity = V2d(1.0, 0.0))
    val behaviors = List(onlyWhen("healthy")(Move(V2d(2.0, 0.0)), Move(V2d(0.0, 3.0))))
    steps(configWith(List(mover, drifter), behaviors = behaviors)).environment.agents.map(_.position) shouldBe
      List(P2d(12.0, 13.0), P2d(51.0, 10.0))

  it should "let the boundary policy resolve a movement crossing the border" in:
    val config = configWith(List(agentAt(0, 98.0)), behaviors = List(always(Move(V2d(5.0, 0.0)))))
    val bounced = steps(config).environment.agents.head
    bounced.position shouldBe P2d(100.0, 10.0)
    bounced.velocity shouldBe V2d(-5.0, 0.0)

  "The population phase" should "give every newborn a fresh identifier and place it where its parent has moved" in:
    val parenting = always(Move(V2d(1.0, 0.0)), Spawn("first"), Spawn("second"))
    val state = steps(configWith(List(agentAt(0, 10.0)), behaviors = List(parenting)))
    state.environment.agents.map(_.id.value) shouldBe List(0, 1, 2)
    state.environment.agents.map(_.position).distinct shouldBe List(P2d(11.0, 10.0))
    state.nextId shouldBe 3

  it should "remove an agent asking to die, keeping the agents it spawned in the same tick" in:
    val config = configWith(List(agentAt(0, 10.0)), behaviors = List(always(Spawn("child"), Die())))
    steps(config).environment.agents.map(_.state) shouldBe List("child")

  it should "deliver a told event to the addressed agent only" in:
    val listenerMemory = memoryMock()
    val bystanderMemory = mock(classOf[Memory])
    val teller = agentAt(0, 10.0, state = "teller")
    val listener = withMemory(1, 12.0, listenerMemory)
    val bystander = withMemory(2, 14.0, bystanderMemory)
    steps(configWith(List(teller, listener, bystander), behaviors = List(onlyWhen("teller")(Tell(listener.id, event)))))
    verify(listenerMemory).remember(0, event)
    verifyNoInteractions(bystanderMemory)

  "The residency tracking" should "count the consecutive ticks spent inside a poi, resetting the count on leaving" in:
    val poi = POI(PoiId(0), "home", P2d(10.0, 10.0), 5.0)
    val resident = agentAt(0, 10.0)
    val leaver = agentAt(1, 12.0, state = "leaving")
    val leaving = Behavior(Some("leaving"))(ctx =>
      if ctx.residency.ticksIn(poi.id) >= 2 then List(Move(V2d(50.0, 0.0))) else List(Move(V2d.zero))
    )
    val state = steps(configWith(List(resident, leaver), behaviors = List(leaving), poiList = List(poi)), times = 3)
    state.residencyOf(resident.id).ticksIn(poi.id) shouldBe 3
    state.residencyOf(leaver.id).ticksIn(poi.id) shouldBe 0
