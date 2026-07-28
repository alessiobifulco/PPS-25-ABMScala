import domain.*
import engine.{SimulationConfig, SimulationEngine}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimulationEngineTest extends AnyFlatSpec with Matchers:

  private val space = RectangularSpace(100, 100)

  private val toOrigin = new BoundaryPolicy:
    def apply(candidatePosition: P2d, currentVelocity: V2d, space: Space): (P2d, V2d) = (P2d(0, 0), V2d.zero)

  private def agent(id: Int, position: P2d, velocity: V2d = V2d.zero, state: String = "healthy") =
    Agent(AgentId(id), position, velocity, state)

  private def config(
      agents: List[Agent[String]],
      behavior: Behavior[String] = Behavior(_ => Nil),
      boundaryPolicy: BoundaryPolicy = BoundaryPolicy.bounce
  ) = SimulationConfig(Environment(space, agents, boundaryPolicy), behavior, perceptionRadius = 10)

  private def tickOnce(config: SimulationConfig[String]): List[Agent[String]] = SimulationEngine
    .tick(SimulationEngine.init(config), config).environment.agents

  "init" should "start at tick zero with the configured agents" in:
    val state = SimulationEngine.init(config(List(agent(1, P2d(10, 10)))))
    state.tick shouldBe 0
    state.environment.agents shouldBe List(agent(1, P2d(10, 10)))

  "tick" should "advance the counter" in:
    val configuration = config(List(agent(1, P2d(10, 10))))
    SimulationEngine.tick(SimulationEngine.init(configuration), configuration).tick shouldBe 1

  "an agent producing a Move" should "adopt that velocity and be displaced by it" in:
    tickOnce(config(List(agent(1, P2d(10, 10))), Behavior(_ => List(Move(V2d(2, 0)))))) shouldBe
      List(agent(1, P2d(12, 10), V2d(2, 0)))

  "an agent producing no Move" should "keep its previous velocity" in:
    tickOnce(config(List(agent(1, P2d(10, 10), V2d(1, 1))))) shouldBe List(agent(1, P2d(11, 11), V2d(1, 1)))

  "a Move with zero velocity" should "stop the agent" in:
    tickOnce(config(List(agent(1, P2d(10, 10), V2d(1, 1))), Behavior(_ => List(Move(V2d.zero))))) shouldBe
      List(agent(1, P2d(10, 10), V2d.zero))

  "multiple Moves" should "sum vectorially" in:
    val steering = Behavior[String](_ => List(Move(V2d(1, 0)), Move(V2d(0, 2))))
    tickOnce(config(List(agent(1, P2d(10, 10))), steering)) shouldBe List(agent(1, P2d(11, 12), V2d(1, 2)))

  "the boundary policy" should "resolve the candidate position and velocity" in:
    val configuration = config(List(agent(1, P2d(10, 10))), Behavior(_ => List(Move(V2d(5, 5)))), toOrigin)
    tickOnce(configuration) shouldBe List(agent(1, P2d(0, 0), V2d.zero))

  "the context" should "carry only the neighbors within the perception radius" in:
    val countNeighbors = Behavior[String](ctx => List(Move(V2d(ctx.neighbors.size, 0))))
    val agents = List(agent(1, P2d(10, 10)), agent(2, P2d(15, 10)), agent(3, P2d(90, 90)))
    tickOnce(config(agents, countNeighbors)).map(_.velocity) shouldBe List(V2d(1, 0), V2d(1, 0), V2d(0, 0))

  "an interaction rule" should "replace the state of the agents it matches" in:
    val contagion = InteractionRule[String](ctx => Option.when(ctx.neighbors.exists(_.state == "infected"))("infected"))
    val agents = List(agent(1, P2d(10, 10)), agent(2, P2d(15, 10), state = "infected"))
    val configuration = config(agents).copy(rule = contagion)
    tickOnce(configuration).map(_.state) shouldBe List("infected", "infected")

  "the default rule" should "leave every state unchanged" in:
    tickOnce(config(List(agent(1, P2d(10, 10))))).map(_.state) shouldBe List("healthy")

  "the rules" should "read the pre-movement snapshot" in:
    val contagion = InteractionRule[String](ctx => Option.when(ctx.neighbors.exists(_.state == "infected"))("infected"))
    val flee = Behavior[String](ctx => List(Move(if ctx.focus.id == AgentId(1) then V2d(-9, 0) else V2d(9, 0))))
    val agents = List(agent(1, P2d(50, 50)), agent(2, P2d(55, 50), state = "infected"))
    val moved = tickOnce(config(agents, flee).copy(rule = contagion))
    moved.map(_.state) shouldBe List("infected", "infected")
    (moved(1).position - moved.head.position).length should be > 10.0
