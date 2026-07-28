import domain.{AgentId, Move, ShareMemory, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActionTest extends AnyFlatSpec with Matchers:

  "Move" should "hold a velocity" in:
    Move[String](V2d(1, 2)).velocity shouldBe V2d(1, 2)

  "ShareMemory" should "hold a target id and an event" in:
    val action = ShareMemory[String](AgentId(2), "encountered agent 5")
    action.targetId shouldBe AgentId(2)
    action.event shouldBe "encountered agent 5"
