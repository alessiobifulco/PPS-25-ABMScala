import domain.{AgentId, V2d}
import domain.Action
import domain.{Move, Nudge, ShareMemory, MultiAction}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActionTest extends AnyFlatSpec with Matchers:

  "Move" should "hold a velocity" in:
    val m = Move[String](V2d(1, 2))
    m.velocity shouldBe V2d(1, 2)

  it should "be equal to another Move with the same velocity" in:
    Move[String](V2d(1, 2)) shouldBe Move[String](V2d(1, 2))

  "Nudge" should "hold a target id and a transform function" in:
    val n = Nudge[Int](AgentId(1), x => x + 1)
    n.targetId shouldBe AgentId(1)
    n.transform(4) shouldBe 5

  it should "be equal to another Nudge with the same target id and transform" in:
    val f: Int => Int = _ + 1
    Nudge[Int](AgentId(1), f) shouldBe Nudge[Int](AgentId(1), f)

  "ShareMemory" should "hold a target id and an event" in:
    val s = ShareMemory[String](AgentId(2), "encountered agent 5")
    s.targetId shouldBe AgentId(2)
    s.event shouldBe "encountered agent 5"

  it should "be equal to another ShareMemory with the same target id and event" in:
    ShareMemory[String](AgentId(2), "x") shouldBe ShareMemory[String](AgentId(2), "x")

  "MultiAction" should "hold a list of actions" in:
    val ma = MultiAction[String](List(Move(V2d(1, 0)), ShareMemory(AgentId(1), "hi")))
    ma.actions shouldBe List(Move(V2d(1, 0)), ShareMemory(AgentId(1), "hi"))

  "Action.flatten" should "leave a list without MultiAction unchanged" in:
    val actions: List[Action[String]] = List(Move(V2d(1, 0)), ShareMemory(AgentId(1), "hi"))
    Action.flatten(actions) shouldBe actions

  it should "inline nested MultiAction, preserving order" in:
    val nested: List[Action[String]] = List(Move(V2d(1, 0)), MultiAction(List(Move(V2d(0, 1)), Move(V2d(9, 9)))))
    Action.flatten(nested) shouldBe List(Move(V2d(1, 0)), Move(V2d(0, 1)), Move(V2d(9, 9)))
