import domain.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BehaviorTest extends AnyFlatSpec with Matchers:

  private val agent = Agent(AgentId(1), P2d(0.0, 0.0), V2d.zero, "healthy")
  private val ctx = AgentContext(agent, List.empty, 0)
  private val move: Action[String] = Action.Move(V2d(1.0, 0.0))

  "A behavior" should "expose the state it is bound to" in:
    Behavior(Some("healthy"))(_ => List(move)).whenState shouldBe Some("healthy")

  it should "produce the actions of its block" in:
    Behavior(Some("healthy"))(_ => List(move)).actions(ctx) shouldBe List(move)

  it should "apply to an agent in the expected state" in:
    Behavior(Some("healthy"))(_ => List(move)).appliesTo(ctx) shouldBe true

  it should "not apply to an agent in another state" in:
    Behavior(Some("infected"))(_ => List(move)).appliesTo(ctx) shouldBe false

  it should "apply to any state when no state is given" in:
    Behavior(Option.empty[String])(_ => List(move)).appliesTo(ctx) shouldBe true
