import domain.{Agent, AgentId, Bounds, Environment, P2d, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EnvironmentTest extends AnyFlatSpec with Matchers:

  private val bounds = Bounds(width = 100.0, height = 50.0)
  private val radius = 10.0
  private val agent1 = Agent(id = AgentId(1), position = P2d(20.0, 20.0), velocity = V2d.zero, state = "agent-1")
  private val agent2 = Agent(id = AgentId(2), position = P2d(25.0, 20.0), velocity = V2d.zero, state = "agent-2")
  private val distantAgent =
    Agent(id = AgentId(3), position = P2d(80.0, 20.0), velocity = V2d.zero, state = "distant-agent")
  private val environment = Environment(bounds = bounds, agents = List(agent1, agent2, distantAgent))

  "Environment" should "preserve its bounds" in:
    environment.bounds shouldBe bounds

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
