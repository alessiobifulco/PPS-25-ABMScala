import domain.*
import dsl.*
import dsl.DiscreteRules.*
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DiscreteRulesTest extends AnyFlatSpec with Matchers:

  private val focus = Agent(AgentId(0), P2d(10.0, 10.0), V2d.zero, "healthy")
  private val infected = Agent(AgentId(1), P2d(12.0, 10.0), V2d.zero, "infected")
  private val healthy = Agent(AgentId(2), P2d(14.0, 10.0), V2d.zero, "healthy")
  private val ctx = AgentContext(focus, List(infected, healthy), 10)
  private val poi = POI(PoiId(0), "home", P2d(10.0, 10.0), 5.0, 2)

  private def sighting(at: Int): AgentContext[String] =
    val memory = mock(classOf[Memory])
    when(memory.beliefs).thenReturn(List(Belief(MemoryEvent.Sighting(PoiId(0), P2d(10.0, 10.0)), at)))
    AgentContext(focus.withMemory(Some(memory)), List.empty, 10)

  "atLeastNear" should "hold when enough neighbors are in the state" in:
    atLeastNear(1, "infected")(ctx) shouldBe true

  it should "not hold when the neighbors are too few" in:
    atLeastNear(2, "infected")(ctx) shouldBe false

  "exactlyNear" should "hold when the neighbors are exactly the expected ones" in:
    exactlyNear(1, "infected")(ctx) shouldBe true
    exactlyNear(2, "infected")(ctx) shouldBe false

  "fewerNear" should "hold when the neighbors are fewer than expected" in:
    fewerNear(2, "infected")(ctx) shouldBe true
    fewerNear(1, "infected")(ctx) shouldBe false

  "chanceOf" should "never hold with a null probability" in:
    chanceOf[String](0.0)(ctx) shouldBe false

  it should "always hold with a probability of one" in:
    chanceOf[String](1.0)(ctx) shouldBe true

  "inside" should "hold when the agent is within the poi" in:
    inside[String](poi)(ctx) shouldBe true

  it should "not hold when the agent is outside the poi" in:
    inside[String](POI(PoiId(1), "far", P2d(90.0, 90.0), 5.0))(ctx) shouldBe false

  "settledIn" should "hold only after the activation delay" in:
    val residency = Residency.empty.tickFor(poi.id).tickFor(poi.id).tickFor(poi.id)
    settledIn[String](poi)(ctx) shouldBe false
    settledIn[String](poi)(AgentContext(focus, List.empty, 10, residency)) shouldBe true

  "farFrom" should "hold when the target is far enough" in:
    farFrom[String](P2d(30.0, 10.0), 10.0)(ctx) shouldBe true

  it should "not hold when the target is close" in:
    farFrom[String](P2d(15.0, 10.0), 10.0)(ctx) shouldBe false

  "recentlySighted" should "hold when a sighting is recent enough" in:
    recentlySighted[String](5)(sighting(8)) shouldBe true

  it should "not hold when the sighting is too old" in:
    recentlySighted[String](1)(sighting(8)) shouldBe false

  "nothingSightedIn" should "hold when the sighting is old enough" in:
    nothingSightedIn[String](1)(sighting(8)) shouldBe true

  it should "not hold when a sighting is recent" in:
    nothingSightedIn[String](5)(sighting(8)) shouldBe false

  "and" should "hold only when both conditions hold" in:
    (atLeastNear(1, "infected") and farFrom[String](P2d(30.0, 10.0), 10.0))(ctx) shouldBe true
    (atLeastNear(1, "infected") and farFrom[String](P2d(15.0, 10.0), 10.0))(ctx) shouldBe false

  "or" should "hold when at least one condition holds" in:
    (atLeastNear(5, "infected") or atLeastNear(1, "infected"))(ctx) shouldBe true
    (atLeastNear(5, "infected") or atLeastNear(3, "infected"))(ctx) shouldBe false

  "iff" should "register a rule bound to the starting state" in:
    given builder: RulesBuilder[String] = RulesBuilder[String]()
    "infected" whenAgentIs "healthy" iff atLeastNear(1, "infected")
    builder.rules.head.whenState shouldBe Some("healthy")
    builder.rules.head.newState(ctx) shouldBe "infected"

  it should "register a rule applied only when the condition holds" in:
    given builder: RulesBuilder[String] = RulesBuilder[String]()
    "infected" whenAgentIs "healthy" iff atLeastNear(3, "infected")
    builder.rules.head.appliesTo(ctx) shouldBe false
