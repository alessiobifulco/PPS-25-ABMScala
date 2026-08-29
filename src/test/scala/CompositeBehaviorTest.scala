import domain.*
import dsl.*
import dsl.CompositeBehavior.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CompositeBehaviorTest extends AnyFlatSpec with Matchers:

  private val focus = Agent(AgentId(0), P2d(50.0, 50.0), V2d(1.0, 0.0), "bird")
  private val ahead = Agent(AgentId(1), P2d(60.0, 50.0), V2d(1.0, 0.0), "bird")
  private val behind = Agent(AgentId(2), P2d(10.0, 50.0), V2d(-1.0, 0.0), "bird")
  private val wolf = Agent(AgentId(3), P2d(60.0, 50.0), V2d.zero, "wolf")
  private val ctx = AgentContext(focus, List(ahead), 0)

  private def velocityOf(actions: List[Action[String]]): V2d = actions.collect { case Action.Move(v) => v }.head

  "A flock behavior" should "produce a single move action" in:
    follow[String](_ == _)(ctx) should have size 1

  it should "move at the configured speed" in:
    velocityOf((follow[String](_ == _) movingAt 3.0)(ctx)).length shouldBe 3.0 +- 0.0001

  it should "keep its own heading when it has no neighbors" in:
    velocityOf(follow[String](_ == _)(AgentContext(focus, List.empty, 0))) shouldBe V2d(1.0, 0.0)

  it should "steer towards the agents it follows" in:
    velocityOf(follow[String](_ == _)(AgentContext(focus, List(behind), 0))).x should be < 0.0

  it should "escape from the agents it avoids" in:
    val flock = follow[String](_ == _) avoid (_ != _)
    velocityOf(flock(AgentContext(focus, List(wolf), 0))).x should be < 0.0

  it should "escape from the neighbors that are too close" in:
    val flock = follow[String](_ == _) keepingApart 20.0 withSeparation 5.0
    velocityOf(flock(ctx)).x should be < 0.0

  it should "follow only its own heading when cohesion and alignment are disabled" in:
    val flock = follow[String](_ == _) withCohesion 0.0 withAlignment 0.0 withHeading 1.0
    velocityOf(flock(AgentContext(focus, List(behind), 0))) shouldBe V2d(1.0, 0.0)
