import domain.*
import dsl.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RulesBuilderTest extends AnyFlatSpec with Matchers:

  private val first = InteractionRule(Some("healthy"), (_: AgentContext[String]) => true)(_ => "infected")
  private val second = InteractionRule(Some("infected"), (_: AgentContext[String]) => true)(_ => "dead")

  "A rules builder" should "start with no rules" in:
    RulesBuilder[String]().rules shouldBe List.empty

  it should "collect the added rules" in:
    val builder = RulesBuilder[String]()
    builder.add(first)
    builder.rules shouldBe List(first)

  it should "keep the rules in the order they are added" in:
    val builder = RulesBuilder[String]()
    builder.add(first)
    builder.add(second)
    builder.rules shouldBe List(first, second)
