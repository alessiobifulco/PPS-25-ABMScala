import domain.{
  Agent,
  AgentId,
  FilteredNeighborStrategy,
  GridNeighborStrategy,
  NeighborStrategy,
  P2d,
  SpatialHashNeighborStrategy,
  V2d
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NeighborStrategyAdvancedTest extends AnyFlatSpec with Matchers:
  private val agent1 = Agent(id = AgentId(1), position = P2d(20.0, 20.0), velocity = V2d.zero, state = "agent-1")
  private val agent2 = Agent(id = AgentId(2), position = P2d(25.0, 20.0), velocity = V2d.zero, state = "agent-2")
  private val agentOnRadius =
    Agent(id = AgentId(3), position = P2d(30.0, 20.0), velocity = V2d.zero, state = "agent-on-radius")
  private val distantAgent =
    Agent(id = AgentId(4), position = P2d(80.0, 20.0), velocity = V2d.zero, state = "distant-agent")
  private val agents = List(agent1, agent2, agentOnRadius, distantAgent)
  private val radius = 10.0
  private val expectedNeighbours = Set(agent2, agentOnRadius)

  "GridNeighborStrategy" should "find the correct neighbours" in:
    val strategy = new GridNeighborStrategy[String](cellSize = 10.0)
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours.toSet shouldBe expectedNeighbours

  "SpatialHashNeighborStrategy" should "find the correct neighbours" in:
    val strategy = new SpatialHashNeighborStrategy[String](cellSize = 10.0)
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours.toSet shouldBe expectedNeighbours

  "FilteredNeighborStrategy" should "filter neighbours using a predicate" in:
    val strategy =
      FilteredNeighborStrategy(base = NeighborStrategy.bruteForce[String], predicate = _.state == "agent-2")
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours shouldBe List(agent2)

  it should "limit the number of returned neighbours" in:
    val strategy = FilteredNeighborStrategy(
      base = NeighborStrategy.bruteForce[String],
      predicate = _ => true,
      maximumResults = Some(1)
    )
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours.size shouldBe 1

  it should "reject a negative maximum result limit" in:
    an[IllegalArgumentException] should be thrownBy:
      FilteredNeighborStrategy(
        base = NeighborStrategy.bruteForce[String],
        predicate = _ => true,
        maximumResults = Some(-1)
      )

  it should "reject a non-positive cell size" in:
    an[IllegalArgumentException] should be thrownBy:
      new GridNeighborStrategy[String](0.0)
    an[IllegalArgumentException] should be thrownBy:
      new SpatialHashNeighborStrategy[String](-1.0)
