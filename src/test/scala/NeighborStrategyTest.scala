import domain.{Agent, AgentId, NeighborStrategy, P2d, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NeighborStrategyTest extends AnyFlatSpec with Matchers:
  private val radius = 10.0
  private val agent1 = Agent(id = AgentId(1), position = P2d(20.0, 20.0), velocity = V2d.zero, state = "agent-1")
  private val agent2 = Agent(id = AgentId(2), position = P2d(25.0, 20.0), velocity = V2d.zero, state = "agent-2")
  private val agentOnRadius =
    Agent(id = AgentId(3), position = P2d(30.0, 20.0), velocity = V2d.zero, state = "agent-on-radius")
  private val distantAgent =
    Agent(id = AgentId(4), position = P2d(80.0, 20.0), velocity = V2d.zero, state = "distant-agent")
  private val agents = List(agent1, agent2, agentOnRadius, distantAgent)
  private val strategy = NeighborStrategy.bruteForce[String]

  "BruteForceNeighborStrategy" should "find agents inside the given radius" in:
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours should contain(agent2)

  it should "exclude the focal agent" in:
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours should not contain agent1

  it should "exclude agents outside the given radius" in:
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours should not contain distantAgent

  it should "include an agent exactly on the radius" in:
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours should contain(agentOnRadius)

  it should "return all matching neighbours in their original order" in:
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours shouldBe List(agent2, agentOnRadius)

  it should "return an empty list when no neighbours exist" in:
    val neighbours = strategy.neighborsOf(distantAgent, agents, radius)
    neighbours shouldBe empty

  it should "return no neighbours with zero radius" in:
    val neighbours = strategy.neighborsOf(agent1, agents, radius = 0.0)

    neighbours shouldBe empty

  it should "reject a negative radius" in:
    an[IllegalArgumentException] should be thrownBy:
      strategy.neighborsOf(agent1, agents, radius = -1.0)

  it should "find the same neighbours with the grid strategy" in:
    val strategy = NeighborStrategy.grid[String](10.0)
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours.toSet shouldBe Set(agent2, agentOnRadius)

  it should "find the same neighbours with the spatial hash strategy" in:
    val strategy = NeighborStrategy.spatialHash[String](10.0)
    val neighbours = strategy.neighborsOf(agent1, agents, radius)
    neighbours.toSet shouldBe Set(agent2, agentOnRadius)
