import domain.BoundaryPolicy.bounce
import domain.{
  Agent,
  AgentId,
  BoundaryPolicy,
  CircularSpace,
  Environment,
  NeighborStrategy,
  P2d,
  POI,
  PoiId,
  RectangularSpace,
  V2d
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EnvironmentTest extends AnyFlatSpec with Matchers:

  private val space = RectangularSpace(width = 100.0, height = 50.0)
  private val radius = 10.0
  private val agent1 = Agent(id = AgentId(1), position = P2d(20.0, 20.0), velocity = V2d.zero, state = "agent-1")
  private val agent2 = Agent(id = AgentId(2), position = P2d(25.0, 20.0), velocity = V2d.zero, state = "agent-2")
  private val distantAgent =
    Agent(id = AgentId(3), position = P2d(80.0, 20.0), velocity = V2d.zero, state = "distant-agent")
  private val environment = Environment(space = space, agents = List(agent1, agent2, distantAgent))

  "Environment" should "preserve its space" in:
    environment.space shouldBe space

  it should "contain the given agents" in:
    environment.agents shouldBe List(agent1, agent2, distantAgent)

  it should "return a new environment with different agents" in:
    val newAgents = List(agent1, agent2)
    val updatedEnvironment = environment.withAgents(newAgents)
    updatedEnvironment.agents shouldBe newAgents
    updatedEnvironment should not be theSameInstanceAs(environment)

  it should "not modify the original environment" in:
    val originalAgents = environment.agents
    environment.withAgents(List(agent1))
    environment.agents shouldBe originalAgents

  it should "use BouncePolicy by default" in:
    environment.boundaryPolicy shouldBe BoundaryPolicy.bounce

  it should "find the neighbors of an agent using the provided strategy" in:
    given NeighborStrategy[String] = NeighborStrategy.bruteForce[String]
    val neighbors = environment.neighborsOf(agent1, radius)
    neighbors should contain(agent2)
    neighbors should not contain distantAgent
    neighbors should not contain agent1

  it should "return a neighborhood function using the provided strategy" in:
    given NeighborStrategy[String] = NeighborStrategy.bruteForce[String]
    val getNeighbors = environment.neighborhoods(radius)
    val neighbors = getNeighbors(agent1)
    neighbors should contain(agent2)
    neighbors should not contain distantAgent

  it should "start with no POIs by default" in:
    environment.pois shouldBe Nil

  it should "reject a POI outside the space" in:
    val poi = POI(PoiId(0), "name", P2d(200.0, 200.0), 10.0)
    an[IllegalArgumentException] should be thrownBy:
      Environment(space = space, agents = List.empty, pois = List(poi))
