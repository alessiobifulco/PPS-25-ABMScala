import domain.{Agent, AgentContext, AgentId, Behavior, Choice, Decision, Move, P2d, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BehaviorTest extends AnyFlatSpec with Matchers:

  private def contextOf(state: String): AgentContext[String] =
    AgentContext(Agent(AgentId(1), P2d(0, 0), V2d.zero, state), Nil, 0)

  "Behavior" should "produce the actions returned by its function" in:
    val b = Behavior[String](ctx => List(Move(V2d(1, 1))))
    b(contextOf("idle")) shouldBe List(Move(V2d(1, 1)))

  it should "have access to the focus agent through the context" in:
    val b = Behavior[String](ctx => if ctx.focus.state == "fast" then List(Move(V2d(5, 0))) else List(Move(V2d(1, 0))))
    b(contextOf("fast")) shouldBe List(Move(V2d(5, 0)))
    b(contextOf("slow")) shouldBe List(Move(V2d(1, 0)))

  "Behavior.fromDecision" should "produce the actions of the first matching choice" in:
    val decision = Decision[String](
      List(Choice(_.focus.state == "fast", _ => List(Move(V2d(5, 0)))), Choice(_ => true, _ => List(Move(V2d(1, 0)))))
    )
    val b = Behavior.fromDecision(decision)

    b(contextOf("fast")) shouldBe List(Move(V2d(5, 0)))
    b(contextOf("slow")) shouldBe List(Move(V2d(1, 0)))

  "andThen" should "combine the actions produced by both behaviors" in:
    val b1 = Behavior[String](_ => List(Move(V2d(1, 0))))
    val b2 = Behavior[String](_ => List(Move(V2d(0, 1))))

    b1.andThen(b2)(contextOf("idle")) shouldBe List(Move(V2d(1, 0)), Move(V2d(0, 1)))
