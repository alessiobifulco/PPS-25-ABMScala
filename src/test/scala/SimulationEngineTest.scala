import domain.*
import engine.*
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

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

  private def step(config: SimulationConfig[String]): SimulationState[String] = SimulationEngine
    .tick(SimulationEngine.init(config), config)

  "The engine" should "start at tick zero with the initial agents" in:
    val state = SimulationEngine.init(configWith())
    state.tick shouldBe 0
    state.environment.agents shouldBe List(agent)

  it should "start from an id greater than the ones already used" in:
    SimulationEngine.init(configWith()).nextId shouldBe 1

  it should "increase the tick at every step" in:
    step(configWith()).tick shouldBe 1

  it should "move the agents following their behavior" in:
    val behavior = Behavior(Option.empty[String])(_ => List(Action.Move(V2d(2.0, 0.0))))
    step(configWith(behaviors = List(behavior))).environment.agents.head.position shouldBe P2d(12.0, 10.0)

  it should "keep the current velocity when no move is produced" in:
    val moving = Agent(AgentId(0), P2d(10.0, 10.0), V2d(1.0, 0.0), "healthy")
    step(configWith(agents = List(moving))).environment.agents.head.position shouldBe P2d(11.0, 10.0)

  it should "change the state of the agents following the rules" in:
    val rule = InteractionRule(Some("healthy"), (_: AgentContext[String]) => true)(_ => "infected")
    step(configWith(rules = List(rule))).environment.agents.head.state shouldBe "infected"

  it should "remove the agents that die" in:
    val behavior = Behavior(Option.empty[String])(_ => List(Action.Die()))
    step(configWith(behaviors = List(behavior))).environment.agents shouldBe List.empty

  it should "add the spawned agents with a fresh id" in:
    val behavior = Behavior(Option.empty[String])(_ => List(Action.Spawn("child")))
    val agents = step(configWith(behaviors = List(behavior))).environment.agents
    agents should have size 2
    agents.last.id shouldBe AgentId(1)
    agents.last.state shouldBe "child"

  it should "store the remembered events in the memory of the agent" in:
    val memory = mock(classOf[Memory])
    when(memory.remember(0, event)).thenReturn(memory)
    val remembering = Agent(AgentId(0), P2d(10.0, 10.0), V2d.zero, "healthy", Some(memory))
    val behavior = Behavior(Option.empty[String])(_ => List(Action.Remember(event)))
    step(configWith(behaviors = List(behavior), agents = List(remembering)))
    verify(memory).remember(0, event)

  it should "deliver a told event to the target agent" in:
    val memory = mock(classOf[Memory])
    when(memory.remember(0, event)).thenReturn(memory)
    val teller = Agent(AgentId(0), P2d(10.0, 10.0), V2d.zero, "teller")
    val listener = Agent(AgentId(1), P2d(12.0, 10.0), V2d.zero, "listener", Some(memory))
    val behavior = Behavior(Option.empty[String])(ctx =>
      if ctx.focus.id == teller.id then List(Action.Tell(listener.id, event)) else List.empty
    )
    step(configWith(behaviors = List(behavior), agents = List(teller, listener)))
    verify(memory).remember(0, event)

  it should "count the ticks spent by an agent inside a poi" in:
    val poi = POI(PoiId(0), "home", P2d(10.0, 10.0), 5.0)
    step(configWith(poiList = List(poi))).residencyOf(agent.id).ticksIn(poi.id) shouldBe 1

  it should "reset the residency when the agent leaves the poi" in:
    val poi = POI(PoiId(0), "home", P2d(10.0, 10.0), 5.0)
    val behavior = Behavior(Option.empty[String])(_ => List(Action.Move(V2d(50.0, 0.0))))
    step(configWith(behaviors = List(behavior), poiList = List(poi))).residencyOf(agent.id).ticksIn(poi.id) shouldBe 0

  it should "let an agent be tracked between two ticks" in:
    val config = configWith(behaviors = List(Behavior(Option.empty[String])(_ => List(Action.Move(V2d(1.0, 0.0))))))
    val start = SimulationEngine.init(config)
    val end = (1 to 3).foldLeft(start)((state, _) => SimulationEngine.tick(state, config))
    start.environment.agents.head.position shouldBe P2d(10.0, 10.0)
    end.tick shouldBe 3
    end.environment.agents.head.position shouldBe P2d(13.0, 10.0)

  it should "evolve in the same way from the same initial state" in:
    val config = configWith(behaviors = List(Behavior(Option.empty[String])(_ => List(Action.Move(V2d(1.0, 0.0))))))
    val first = SimulationEngine.tick(SimulationEngine.init(config), config)
    val second = SimulationEngine.tick(SimulationEngine.init(config), config)
    first.environment.agents.map(_.position) shouldBe second.environment.agents.map(_.position)
