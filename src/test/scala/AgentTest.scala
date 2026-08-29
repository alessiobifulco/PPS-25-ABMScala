import domain.*
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AgentTest extends AnyFlatSpec with Matchers:

  private val agent = Agent(AgentId(1), P2d(10.0, 20.0), V2d(1.0, 0.0), "healthy")

  "An agent" should "keep the values it was created with" in:
    agent.id shouldBe AgentId(1)
    agent.position shouldBe P2d(10.0, 20.0)
    agent.velocity shouldBe V2d(1.0, 0.0)
    agent.state shouldBe "healthy"

  it should "have no memory by default" in:
    agent.memory shouldBe None

  it should "update its motion keeping the rest unchanged" in:
    val moved = agent.withMotion(P2d(30.0, 40.0), V2d(0.0, 1.0))
    moved.position shouldBe P2d(30.0, 40.0)
    moved.velocity shouldBe V2d(0.0, 1.0)
    moved.id shouldBe agent.id
    moved.state shouldBe agent.state

  it should "update its state keeping the rest unchanged" in:
    val infected = agent.withState("infected")
    infected.state shouldBe "infected"
    infected.position shouldBe agent.position
    infected.velocity shouldBe agent.velocity

  it should "not modify the original agent" in:
    agent.withState("infected")
    agent.state shouldBe "healthy"

  it should "remember nothing when it has no memory" in:
    agent.remembers shouldBe List.empty

  it should "expose the beliefs stored in its memory" in:
    val belief = Belief(MemoryEvent.Encounter(AgentId(2), true), 3)
    val memory = mock(classOf[Memory])
    when(memory.beliefs).thenReturn(List(belief))
    agent.withMemory(Some(memory)).remembers shouldBe List(belief)
