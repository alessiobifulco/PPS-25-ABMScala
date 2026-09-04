import domain.*
import dsl.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BehaviorsBuilderTest extends AnyFlatSpec with Matchers:

  private val first = Behavior(Some("healthy"))(_ => List.empty)
  private val second = Behavior(Some("infected"))(_ => List.empty)

  "A behaviors builder" should "start with no behaviors" in:
    BehaviorsBuilder[String]().behaviors shouldBe List.empty

  it should "collect the added behaviors" in:
    val builder = BehaviorsBuilder[String]()
    builder.add(first)
    builder.behaviors shouldBe List(first)

  it should "keep the behaviors in the order they are added" in:
    val builder = BehaviorsBuilder[String]()
    builder.add(first)
    builder.add(second)
    builder.behaviors shouldBe List(first, second)
