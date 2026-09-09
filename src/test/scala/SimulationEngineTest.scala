import domain.*
import domain.Action.*
import engine.*
import org.mockito.ArgumentMatchers.{any, anyInt}
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Integration suite of the simulation engine, the only component interpreting the actions declared by behaviors and
  * the transitions declared by rules. The cases follow the phases of the tick pipeline: perceive, decide, move, grow,
  * deliver.
  *
  * [[Memory]] is the collaborator the engine depends upon, and it is replaced by a test double because it is immutable
  * and every update returns a new instance, which makes the interaction impossible to observe from the outside. It is
  * used as a dummy when the point is that the engine must not touch it, and as a mock returning itself when the calls
  * it receives have to be verified across several ticks.
  *
  * The state type is instantiated to `String` because the engine is parametric in it and no case relies on any of its
  * properties other than equality.
  */
class SimulationEngineTest extends AnyFlatSpec with Matchers:

  private val space = RectangularSpace(100.0, 100.0)
  private val agent = Agent(AgentId(0), P2d(10.0, 10.0), V2d.zero, "healthy")
  private val event = MemoryEvent.Encounter(AgentId(1), true)

  private def configWith(
      behaviors: List[Behavior[String]] = List.empty,
      rules: List[InteractionRule[String]] = List.empty,
      agents: List[Agent[String]] = List(agent),
      poiList: List[POI] = List.empty
  ): SimulationConfig[String] =
    SimulationConfig(Environment(space, agents, BoundaryPolicy.bounce, poiList), behaviors, 20.0, rules)

  private def step(config: SimulationConfig[String]): SimulationState[String] = steps(config, 1)

  private def steps(config: SimulationConfig[String], times: Int): SimulationState[String] = (1 to times)
    .foldLeft(SimulationEngine.init(config))((state, _) => SimulationEngine.tick(state, config))

  /** A behavior applying to every agent, whatever its state. */
  private def always(actions: Action[String]*): Behavior[String] = Behavior(Option.empty[String])(_ => actions.toList)

  /** A behavior applying only to the agents holding the given state. */
  private def onlyWhen(state: String)(actions: Action[String]*): Behavior[String] =
    Behavior(Some(state))(_ => actions.toList)

  /** A memory double returning itself on every update, so that the same instance stays installed on the agent instead
    * of being replaced by the one a real memory would return.
    */
  private def memoryDouble(): Memory =
    val memory = mock(classOf[Memory])
    when(memory.remember(anyInt(), any(classOf[MemoryEvent]))).thenReturn(memory)
    memory

  private def agentWith(id: Int, memory: Memory): Agent[String] =
    Agent(AgentId(id), P2d(10.0 + id * 2, 10.0), V2d.zero, "healthy", Some(memory))

  "The engine" should "start from a pristine state holding the initial population" in:
    val state = SimulationEngine.init(configWith())
    state.tick shouldBe 0
    state.environment.agents shouldBe List(agent)

  it should "leave motion and state untouched when no behavior and no rule apply" in:
    val resting = step(configWith()).environment.agents.head
    resting.position shouldBe P2d(10.0, 10.0)
    resting.state shouldBe "healthy"

  it should "leave the state it was given untouched" in:
    val config = configWith(behaviors = List(always(Move(V2d(1.0, 0.0)))))
    val start = SimulationEngine.init(config)
    SimulationEngine.tick(start, config)
    start.tick shouldBe 0
    start.environment.agents.head.position shouldBe P2d(10.0, 10.0)

  "The perception phase" should "offer only the agents falling inside the perception radius" in:
    val near = Agent(AgentId(1), P2d(15.0, 10.0), V2d.zero, "healthy")
    val far = Agent(AgentId(2), P2d(90.0, 10.0), V2d.zero, "healthy")
    val counting = Behavior(Option.empty[String])(ctx => List(Move(V2d(ctx.neighbors.size.toDouble, 0.0))))
    step(configWith(behaviors = List(counting), agents = List(agent, near, far))).environment.agents.head
      .position shouldBe P2d(11.0, 10.0)

  "The decision phase" should "fire only the first applicable behavior" in:
    val behaviors = List(always(Move(V2d(1.0, 0.0))), always(Move(V2d(0.0, 1.0))))
    step(configWith(behaviors = behaviors)).environment.agents.head.position shouldBe P2d(11.0, 10.0)

  it should "skip a behavior whose activation state does not match" in:
    val behaviors = List(onlyWhen("infected")(Move(V2d(1.0, 0.0))), always(Move(V2d(0.0, 1.0))))
    step(configWith(behaviors = behaviors)).environment.agents.head.position shouldBe P2d(10.0, 11.0)

  it should "fire only the first applicable rule" in:
    val rules = List(
      InteractionRule(Some("healthy"), (_: AgentContext[String]) => true)(_ => "infected"),
      InteractionRule(Some("healthy"), (_: AgentContext[String]) => true)(_ => "immune")
    )
    step(configWith(rules = rules)).environment.agents.head.state shouldBe "infected"

  it should "skip a rule whose contextual condition is not satisfied" in:
    val rules = List(
      InteractionRule(Some("healthy"), (_: AgentContext[String]) => false)(_ => "infected"),
      InteractionRule(Option.empty[String], (_: AgentContext[String]) => true)(_ => "immune")
    )
    step(configWith(rules = rules)).environment.agents.head.state shouldBe "immune"

  "The movement resolution" should "sum the velocities requested within the same tick" in:
    step(configWith(behaviors = List(always(Move(V2d(2.0, 0.0)), Move(V2d(0.0, 3.0)))))).environment.agents.head
      .position shouldBe P2d(12.0, 13.0)

  it should "let the boundary policy resolve a movement crossing the border" in:
    val atBorder = Agent(AgentId(0), P2d(98.0, 10.0), V2d.zero, "healthy")
    val bounced = step(configWith(behaviors = List(always(Move(V2d(5.0, 0.0)))), agents = List(atBorder))).environment
      .agents.head
    bounced.position shouldBe P2d(100.0, 10.0)
    bounced.velocity shouldBe V2d(-5.0, 0.0)

  "The population lifecycle" should "remove an agent asking to die" in:
    step(configWith(behaviors = List(always(Die())))).environment.agents shouldBe empty

  it should "admit a newborn carrying the requested state and a fresh identifier" in:
    val agents = step(configWith(behaviors = List(always(Spawn("child"))))).environment.agents
    agents should have size 2
    agents.last.id shouldBe AgentId(1)
    agents.last.state shouldBe "child"

  it should "give a distinct identifier to every agent spawned in the same tick" in:
    val state = step(configWith(behaviors = List(always(Spawn("first"), Spawn("second")))))
    state.environment.agents.map(_.id.value) shouldBe List(0, 1, 2)
    state.nextId shouldBe 3

  "The memory update" should "never touch the memory of an agent that has nothing to remember" in:
    val dummy = mock(classOf[Memory])
    step(configWith(behaviors = List(always(Move(V2d(1.0, 0.0)))), agents = List(agentWith(0, dummy))))
    verifyNoInteractions(dummy)

  it should "record the event the agent asked to remember, stamped with the current tick" in:
    val memory = memoryDouble()
    step(configWith(behaviors = List(always(Remember(event))), agents = List(agentWith(0, memory))))
    verify(memory).remember(0, event)

  it should "record one event per tick for as long as the behavior asks for it" in:
    val memory = memoryDouble()
    steps(configWith(behaviors = List(always(Remember(event))), agents = List(agentWith(0, memory))), 3)
    verify(memory, times(3)).remember(anyInt(), any(classOf[MemoryEvent]))

  it should "deliver a told event to the addressed agent only" in:
    val listenerMemory = memoryDouble()
    val bystanderMemory = memoryDouble()
    val teller = Agent(AgentId(0), P2d(10.0, 10.0), V2d.zero, "teller")
    val listener = agentWith(1, listenerMemory)
    val bystander = agentWith(2, bystanderMemory)
    val telling = Behavior(Option.empty[String])(ctx =>
      if ctx.focus.id == teller.id then List(Tell(listener.id, event)) else List.empty
    )
    step(configWith(behaviors = List(telling), agents = List(teller, listener, bystander)))
    verify(listenerMemory).remember(0, event)
    verifyNoInteractions(bystanderMemory)

  "The residency tracking" should "count the consecutive ticks spent inside a poi" in:
    val poi = POI(PoiId(0), "home", P2d(10.0, 10.0), 5.0)
    steps(configWith(poiList = List(poi)), 3).residencyOf(agent.id).ticksIn(poi.id) shouldBe 3

  it should "reset the count as soon as the agent leaves the poi" in:
    val poi = POI(PoiId(0), "home", P2d(10.0, 10.0), 5.0)
    step(configWith(behaviors = List(always(Move(V2d(50.0, 0.0)))), poiList = List(poi))).residencyOf(agent.id)
      .ticksIn(poi.id) shouldBe 0
