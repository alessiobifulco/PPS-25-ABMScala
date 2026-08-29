import domain.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActionTest extends AnyFlatSpec with Matchers:

  private val event = MemoryEvent.Encounter(AgentId(2), true)

  "A move action" should "carry the requested velocity" in:
    val action: Action[String] = Action.Move(V2d(1.0, 2.0))
    action shouldBe Action.Move(V2d(1.0, 2.0))

  "A remember action" should "carry the event to store" in:
    val action: Action[String] = Action.Remember(event)
    action shouldBe Action.Remember(event)

  "A tell action" should "carry the target and the event" in:
    val action: Action[String] = Action.Tell(AgentId(2), event)
    action shouldBe Action.Tell(AgentId(2), event)

  "A spawn action" should "carry the state of the new agent" in:
    val action: Action[String] = Action.Spawn("child")
    action shouldBe Action.Spawn("child")

  "Two death actions" should "be equal" in:
    Action.Die[String]() shouldBe Action.Die[String]()
