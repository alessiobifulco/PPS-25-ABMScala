import domain.{Agent, AgentId, Memory, P2d, V2d, AgentContext}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AgentTest extends AnyFlatSpec with Matchers:

  "Agent" should "hold id, position, velocity and state" in:
    val a = Agent(AgentId(1), P2d(0, 0), V2d.zero, "idle")
    a.id shouldBe AgentId(1)
    a.position shouldBe P2d(0, 0)
    a.velocity shouldBe V2d.zero
    a.state shouldBe "idle"

  it should "accept an explicit memory" in:
    val m = Memory.empty[String]
    val a = Agent(AgentId(1), P2d(0, 0), V2d.zero, "idle", Some(m))
    a.memory shouldBe Some(m)

  it should "be equal to another Agent with the same values" in:
    Agent(AgentId(1), P2d(0, 0), V2d.zero, "idle") shouldBe Agent(AgentId(1), P2d(0, 0), V2d.zero, "idle")

  "AgentId" should "be constructible from an Int" in:
    AgentId(1).value shouldBe 1

  it should "be equal to another AgentId with the same value" in:
    AgentId(1) shouldBe AgentId(1)

  "AgentContext" should "hold the focus agent, its neighbors and the current tick" in:
    val focus = Agent(AgentId(1), P2d(0, 0), V2d.zero, "idle")
    val neighbor = Agent(AgentId(2), P2d(1, 1), V2d.zero, "idle")
    val ctx = AgentContext(focus, List(neighbor), 3)

    ctx.focus shouldBe focus
    ctx.neighbors shouldBe List(neighbor)
    ctx.tick shouldBe 3
