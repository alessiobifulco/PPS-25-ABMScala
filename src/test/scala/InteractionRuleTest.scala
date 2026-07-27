import domain.{Agent, AgentContext, AgentId, InteractionRule, P2d, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class InteractionRuleTest extends AnyFlatSpec with Matchers:

  enum TestState:
    case Healthy, Infected, Immune
  import TestState.*

  val defaultAgent: Agent[TestState] = Agent(AgentId(1), P2d(0, 0), V2d.zero, Healthy)
  val defaultCtx: AgentContext[TestState] = AgentContext(defaultAgent, List.empty, tick = 0)
  val infectedAgent: Agent[TestState] = Agent(AgentId(2), P2d(0, 0), V2d.zero, Infected)
  val infectedCtx: AgentContext[TestState] = AgentContext(infectedAgent, List.empty, tick = 0)

  "InteractionRule.apply" should "wrap a pure function correctly" in:
    val rule = InteractionRule[TestState]: ctx =>
      if ctx.focus.state == Healthy then Some(Infected) else None
    rule(defaultCtx) shouldBe Some(Infected)
    rule(infectedCtx) shouldBe None

  "InteractionRule.firstOf" should "return None if no rules are provided" in:
    val emptyRule = InteractionRule.firstOf[TestState]()
    emptyRule(defaultCtx) shouldBe None

  it should "return None if all rules return None" in:
    val rule1 = InteractionRule[TestState](_ => None)
    val rule2 = InteractionRule[TestState](_ => None)
    val composite = InteractionRule.firstOf(rule1, rule2)
    composite(defaultCtx) shouldBe None

  it should "return the result of the first matching rule based on priority" in:
    val rule1 = InteractionRule[TestState](_ => None)
    val rule2 = InteractionRule[TestState](_ => Some(Infected))
    val rule3 = InteractionRule[TestState](_ => Some(Immune))
    val composite = InteractionRule.firstOf(rule1, rule2, rule3)
    composite(defaultCtx) shouldBe Some(Infected)

  it should "short-circuit and not evaluate subsequent rules once a match is found" in:
    var rule2Evaluated = false
    val rule1 = InteractionRule[TestState](_ => Some(Immune))
    val rule2 = InteractionRule[TestState]: _ =>
      rule2Evaluated = true
      Some(Infected)
    val composite = InteractionRule.firstOf(rule1, rule2)
    composite(defaultCtx) shouldBe Some(Immune)
    rule2Evaluated shouldBe false
