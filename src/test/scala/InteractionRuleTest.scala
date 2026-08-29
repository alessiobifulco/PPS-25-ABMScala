import domain.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class InteractionRuleTest extends AnyFlatSpec with Matchers:

  private val agent = Agent(AgentId(1), P2d(0.0, 0.0), V2d.zero, "healthy")
  private val ctx = AgentContext(agent, List.empty, 0)
  private val always: Condition[String] = _ => true
  private val never: Condition[String] = _ => false

  "A rule" should "compute the new state of the agent" in:
    InteractionRule(Some("healthy"), always)(_ => "infected").newState(ctx) shouldBe "infected"

  it should "be able to build the new state from the context" in:
    InteractionRule(Some("healthy"), always)(c => s"tick-${c.tick}").newState(ctx) shouldBe "tick-0"

  it should "apply when the state and the condition match" in:
    InteractionRule(Some("healthy"), always)(_ => "infected").appliesTo(ctx) shouldBe true

  it should "not apply when the state does not match" in:
    InteractionRule(Some("infected"), always)(_ => "dead").appliesTo(ctx) shouldBe false

  it should "not apply when the condition is false" in:
    InteractionRule(Some("healthy"), never)(_ => "infected").appliesTo(ctx) shouldBe false

  it should "apply to any state when no state is given" in:
    InteractionRule(Option.empty[String], always)(_ => "infected").appliesTo(ctx) shouldBe true
