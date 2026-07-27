import domain.{Agent, AgentContext, AgentId, Choice, Decision, Move, P2d, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DecisionTest extends AnyFlatSpec with Matchers:

  private def contextOf(state: String): AgentContext[String] =
    AgentContext(Agent(AgentId(1), P2d(0, 0), V2d.zero, state), Nil, 0)

  "Decision" should "produce the actions of the first matching choice" in:
    val decision = Decision[String](List(
      Choice(_.focus.state == "fast", _ => List(Move(V2d(5, 0)))),
      Choice(_.focus.state == "slow", _ => List(Move(V2d(1, 0))))
    ))
    decision.decide(contextOf("fast")) shouldBe List(Move(V2d(5, 0)))
    decision.decide(contextOf("slow")) shouldBe List(Move(V2d(1, 0)))

  it should "respect the order of the choices when more than one matches" in:
    val decision = Decision[String](
      List(Choice(_ => true, _ => List(Move(V2d(1, 1)))), Choice(_ => true, _ => List(Move(V2d(2, 2)))))
    )
    decision.decide(contextOf("idle")) shouldBe List(Move(V2d(1, 1)))

  it should "produce no actions when no choice matches" in:
    val decision = Decision[String](List(Choice(_.focus.state == "fast", _ => List(Move(V2d(5, 0))))))
    decision.decide(contextOf("slow")) shouldBe empty

  it should "produce no actions when it has no choices" in:
    Decision[String](Nil).decide(contextOf("idle")) shouldBe empty

  it should "let a choice compute its actions from the context" in:
    val decision = Decision[String](List(Choice(_ => true, ctx => List(Move(V2d(ctx.neighbors.size, 0))))))
    val focus = Agent(AgentId(1), P2d(0, 0), V2d.zero, "idle")
    val neighbor = Agent(AgentId(2), P2d(1, 1), V2d.zero, "idle")

    decision.decide(AgentContext(focus, List(neighbor, neighbor), 0)) shouldBe List(Move(V2d(2, 0)))
