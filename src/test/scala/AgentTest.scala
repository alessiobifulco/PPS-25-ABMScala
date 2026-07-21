import domain.{P2d, V2d, Agent, Memory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AgentTest extends AnyFlatSpec with Matchers:

  "Agent" should "hold id, position, velocity and state" in:
    val a = Agent(1, P2d(0, 0), V2d.zero, "idle")
    a.id shouldBe 1
    a.position shouldBe P2d(0, 0)
    a.velocity shouldBe V2d.zero
    a.state shouldBe "idle"

  it should "accept an explicit memory" in:
    val m = Memory.empty[String]
    val a = Agent(1, P2d(0, 0), V2d.zero, "idle", Some(m))
    a.memory shouldBe Some(m)

  it should "be equal to another Agent with the same values" in:
    Agent(1, P2d(0, 0), V2d.zero, "idle") shouldBe Agent(1, P2d(0, 0), V2d.zero, "idle")
