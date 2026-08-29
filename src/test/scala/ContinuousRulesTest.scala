import domain.*
import dsl.*
import dsl.ContinuousRules.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ContinuousRulesTest extends AnyFlatSpec with Matchers:

  private case class Opinion(value: Double)

  private given Continuous[Opinion] with
    override def extract(state: Opinion): Double = state.value
    override def update(state: Opinion, value: Double): Opinion = state.copy(value = value)

  private val focus = Agent(AgentId(0), P2d(10.0, 10.0), V2d.zero, Opinion(0.0))
  private val near = Agent(AgentId(1), P2d(12.0, 10.0), V2d.zero, Opinion(10.0))
  private val far = Agent(AgentId(2), P2d(90.0, 10.0), V2d.zero, Opinion(20.0))
  private val ctx = AgentContext(focus, List(near), 0)

  "convergeTowardsAverage" should "register a rule in the builder" in:
    given builder: RulesBuilder[Opinion] = RulesBuilder[Opinion]()
    convergeTowardsAverage[Opinion]()
    builder.rules should have size 1

  it should "move the state towards the average of the neighbors" in:
    given builder: RulesBuilder[Opinion] = RulesBuilder[Opinion]()
    convergeTowardsAverage[Opinion]()
    builder.rules.head.newState(ctx) shouldBe Opinion(10.0)

  it should "converge slowly with a lower rate" in:
    given builder: RulesBuilder[Opinion] = RulesBuilder[Opinion]()
    convergeTowardsAverage[Opinion](atRate = 0.5)
    builder.rules.head.newState(ctx) shouldBe Opinion(5.0)

  it should "consider only the neighbors within the influence radius" in:
    given builder: RulesBuilder[Opinion] = RulesBuilder[Opinion]()
    convergeTowardsAverage[Opinion](within = 10.0)
    builder.rules.head.newState(AgentContext(focus, List(near, far), 0)) shouldBe Opinion(10.0)

  it should "not apply when nobody influences the agent" in:
    given builder: RulesBuilder[Opinion] = RulesBuilder[Opinion]()
    convergeTowardsAverage[Opinion](within = 1.0)
    builder.rules.head.appliesTo(ctx) shouldBe false

  it should "consider only the neighbors accepted by the predicate" in:
    given builder: RulesBuilder[Opinion] = RulesBuilder[Opinion]()
    convergeTowardsAverage[Opinion](among = (a, b) => math.abs(a.value - b.value) <= 1.0)
    builder.rules.head.appliesTo(ctx) shouldBe false
