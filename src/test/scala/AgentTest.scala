//import domain.{Agent, AgentId, Memory, P2d, V2d}
//import org.scalatest.flatspec.AnyFlatSpec
//import org.scalatest.matchers.should.Matchers
//
//class AgentTest extends AnyFlatSpec with Matchers:
//
//  private val memory = Memory.empty[String]
//  private val agent = Agent(AgentId(1), P2d(0, 0), V2d(1, 0), "healthy", Some(memory))
//
//  "An agent" should "expose the values it was built with" in:
//    agent.id shouldBe AgentId(1)
//    agent.position shouldBe P2d(0, 0)
//    agent.velocity shouldBe V2d(1, 0)
//    agent.state shouldBe "healthy"
//    agent.memory shouldBe Some(memory)
//
//  it should "have no memory by default" in:
//    Agent(AgentId(2), P2d(0, 0), V2d.zero, "healthy").memory shouldBe None
//
//  "withMotion" should "replace position and velocity, leaving the rest unchanged" in:
//    agent.withMotion(P2d(3, 4), V2d(0, 1)) shouldBe Agent(AgentId(1), P2d(3, 4), V2d(0, 1), "healthy", Some(memory))
//
//  "withState" should "replace the state, leaving the rest unchanged" in:
//    agent.withState("infected") shouldBe Agent(AgentId(1), P2d(0, 0), V2d(1, 0), "infected", Some(memory))
