import gui.Msg
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MsgTest extends AnyFlatSpec, Matchers:

  "Msg" should "define all expected message cases" in:
    val messages = List(Msg.Tick, Msg.ToggleRun, Msg.RestartAndRun)

    messages should contain allElementsOf List(Msg.Tick, Msg.ToggleRun, Msg.RestartAndRun)
    messages.distinct should have size 3
