import gui.{Msg, Mvu, State}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MvuTest extends AnyFlatSpec with Matchers with GuiFixtures:

  "Mvu.init" should "produce a running model" in { Mvu.init(config).running shouldBe true }

  "Mvu.update" should "not advance tick when paused" in {
    val model = Mvu.init(config).copy(running = false)
    val (newModel, _) = Mvu.update(Msg.Tick).apply(model)
    newModel.state.tick shouldBe 0
  }

  it should "advance tick when running" in {
    val model = Mvu.init(config)
    val (newModel, _) = Mvu.update(Msg.Tick).apply(model)
    newModel.state.tick shouldBe 1
  }

  it should "toggle running from running" in {
    val model = Mvu.init(config)
    val (newModel, _) = Mvu.update(Msg.ToggleRun).apply(model)
    newModel.running shouldBe false
  }

  it should "toggle running from paused" in {
    val model = Mvu.init(config).copy(running = false)
    val (newModel, _) = Mvu.update(Msg.ToggleRun).apply(model)
    newModel.running shouldBe true
  }

  it should "reset tick on RestartAndRun" in {
    val model = Mvu.init(config)
    val (afterTick, _) = Mvu.update(Msg.Tick).apply(model)
    val (restarted, _) = Mvu.update(Msg.RestartAndRun).apply(afterTick)
    restarted.state.tick shouldBe 0
  }

  it should "preserve running state on RestartAndRun" in {
    val model = Mvu.init(config)
    val (restarted, _) = Mvu.update(Msg.RestartAndRun).apply(model)
    restarted.running shouldBe true
  }
