import gui.Msg
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MsgTest extends AnyFlatSpec with Matchers:

  "Msg" should "have Tick case" in { Msg.Tick shouldBe Msg.Tick }

  it should "have ToggleRun case" in { Msg.ToggleRun shouldBe Msg.ToggleRun }

  it should "have Restart case" in { Msg.Restart shouldBe Msg.Restart }
