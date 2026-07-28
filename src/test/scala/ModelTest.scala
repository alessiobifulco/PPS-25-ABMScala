import gui.Model
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ModelTest extends AnyFlatSpec with Matchers with GuiFixtures:

  "Model.from" should "create model with tick 0" in { Model.from(config).state.tick shouldBe 0 }

  it should "start paused" in { Model.from(config).running shouldBe false }

  it should "contain the correct agents" in {
    Model.from(config).state.environment.agents should contain theSameElementsAs List(agentA, agentB)
  }

  it should "store the config" in { Model.from(config).config shouldBe config }
