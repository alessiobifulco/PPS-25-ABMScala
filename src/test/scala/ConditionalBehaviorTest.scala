import domain.*
import dsl.*
import dsl.Chance.*
import dsl.ConditionalBehavior.*
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConditionalBehaviorTest extends AnyFlatSpec with Matchers:

  private val speed = 2.0
  private val focus = Agent(AgentId(0), P2d(10.0, 10.0), V2d(1.0, 0.0), "healthy")
  private val other = Agent(AgentId(1), P2d(12.0, 10.0), V2d.zero, "healthy")
  private val ctx = AgentContext(focus, List.empty, 0)

  "stopMoving" should "produce a null velocity" in:
    stopMoving[String](ctx) shouldBe List(Action.Move(V2d.zero))

  "die" should "produce a death action" in:
    die[String](ctx) shouldBe List(Action.Die())

  "spawn" should "produce the state of the new agent" in:
    spawn[String]("child")(ctx) shouldBe List(Action.Spawn("child"))

  "moveHorizontally" should "keep going right when the agent moves right" in:
    moveHorizontally[String](speed)(ctx) shouldBe List(Action.Move(V2d(speed, 0.0)))

  it should "keep going left when the agent moves left" in:
    val leftward = AgentContext(focus.withMotion(focus.position, V2d(-1.0, 0.0)), List.empty, 0)
    moveHorizontally[String](speed)(leftward) shouldBe List(Action.Move(V2d(-speed, 0.0)))

  "moveTowards" should "head to the target at the given speed" in:
    moveTowards[String](P2d(20.0, 10.0), speed)(ctx) shouldBe List(Action.Move(V2d(2.0, 0.0)))

  "moveAwayFrom" should "head in the opposite direction of the target" in:
    moveAwayFrom[String](P2d(20.0, 10.0), speed)(ctx) shouldBe List(Action.Move(V2d(-2.0, 0.0)))

  "moveTowardsRemembered" should "head to the last remembered sighting" in:
    val memory = mock(classOf[Memory])
    when(memory.sightings).thenReturn(List(Belief(MemoryEvent.Sighting(PoiId(0), P2d(20.0, 10.0)), 1)))
    val remembering = AgentContext(focus.withMemory(Some(memory)), List.empty, 0)
    moveTowardsRemembered[String](speed)(remembering) shouldBe List(Action.Move(V2d(2.0, 0.0)))

  it should "produce nothing when nothing was sighted" in:
    moveTowardsRemembered[String](speed)(ctx) shouldBe List.empty

  "moveAwayFromRemembered" should "escape from the last remembered sighting" in:
    val memory = mock(classOf[Memory])
    when(memory.sightings).thenReturn(List(Belief(MemoryEvent.Sighting(PoiId(0), P2d(20.0, 10.0)), 1)))
    val remembering = AgentContext(focus.withMemory(Some(memory)), List.empty, 0)
    moveAwayFromRemembered[String](speed)(remembering) shouldBe List(Action.Move(V2d(-2.0, 0.0)))

  "rememberSightings" should "remember only the poi containing the agent" in:
    val here = POI(PoiId(0), "home", P2d(10.0, 10.0), 5.0)
    val elsewhere = POI(PoiId(1), "far", P2d(90.0, 90.0), 5.0)
    rememberSightings[String](here, elsewhere)(ctx) shouldBe
      List(Action.Remember(MemoryEvent.Sighting(here.id, here.position)))

  "tellNeighbours" should "send the latest belief to every neighbor" in:
    val belief = Belief(MemoryEvent.Encounter(AgentId(9), true), 2)
    val memory = mock(classOf[Memory])
    when(memory.latest).thenReturn(Some(belief))
    val talkative = AgentContext(focus.withMemory(Some(memory)), List(other), 0)
    tellNeighbours[String](talkative) shouldBe List(Action.Tell(other.id, belief.event))

  it should "produce nothing when the agent has no memory" in:
    tellNeighbours[String](AgentContext(focus, List(other), 0)) shouldBe List.empty

  "learnFromNeighbours" should "remember the most recent belief heard" in:
    val old = Belief(MemoryEvent.Encounter(AgentId(8), true), 1)
    val recent = Belief(MemoryEvent.Encounter(AgentId(9), false), 5)
    val forgetful = mock(classOf[Memory])
    when(forgetful.beliefs).thenReturn(List(old))
    val informed = mock(classOf[Memory])
    when(informed.beliefs).thenReturn(List(recent))
    val neighbors = List(other.withMemory(Some(forgetful)), other.withMemory(Some(informed)))
    learnFromNeighbours[String](AgentContext(focus, neighbors, 0)) shouldBe List(Action.Remember(recent.event))

  it should "produce nothing when no neighbor has beliefs" in:
    learnFromNeighbours[String](AgentContext(focus, List(other), 0)) shouldBe List.empty

  "to" should "concatenate the actions of two sources" in:
    (stopMoving[String] to die[String])(ctx) shouldBe List(Action.Move(V2d.zero), Action.Die())

  "orElse" should "use the second source when the first produces nothing" in:
    val silent: ActionSource[String] = _ => List.empty
    (silent orElse die[String])(ctx) shouldBe List(Action.Die())

  it should "keep the first source when it produces something" in:
    (stopMoving[String] orElse die[String])(ctx) shouldBe List(Action.Move(V2d.zero))

  "onlyIf" should "produce the actions when the condition holds" in:
    (die[String] onlyIf (_ => true))(ctx) shouldBe List(Action.Die())

  it should "produce nothing when the condition does not hold" in:
    (die[String] onlyIf (_ => false))(ctx) shouldBe List.empty

  "vanishingWith" should "add a death when the chance always happens" in:
    (stopMoving[String] vanishingWith chance(1.0))(ctx) shouldBe List(Action.Move(V2d.zero), Action.Die())

  it should "leave the actions untouched when the chance never happens" in:
    (stopMoving[String] vanishingWith chance(0.0))(ctx) shouldBe List(Action.Move(V2d.zero))

  "whenAgentIs" should "register a behavior bound to a state" in:
    given builder: BehaviorsBuilder[String] = BehaviorsBuilder[String]()
    stopMoving[String] whenAgentIs "healthy"
    builder.behaviors.head.whenState shouldBe Some("healthy")
    builder.behaviors.head.actions(ctx) shouldBe List(Action.Move(V2d.zero))

  "asDefault" should "register a behavior applied to any state" in:
    given builder: BehaviorsBuilder[String] = BehaviorsBuilder[String]()
    asDefault(die[String])
    builder.behaviors.head.whenState shouldBe None
    builder.behaviors.head.actions(ctx) shouldBe List(Action.Die())
