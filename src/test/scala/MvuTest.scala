import gui.{Msg, Mvu}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MvuTest extends AnyFlatSpec with Matchers with GuiFixtures:

  "Mvu.init" should "produce a running model" in { Mvu.init(config).running shouldBe true }

  "Mvu.update" should "not advance tick when paused" in {
    val model = Mvu.init(config).copy(running = false)
    Mvu.update(model, Msg.Tick).state.tick shouldBe 0
  }

  it should "advance tick when running" in {
    val model = Mvu.init(config)
    Mvu.update(model, Msg.Tick).state.tick shouldBe 1
  }

  it should "toggle running from running" in { Mvu.update(Mvu.init(config), Msg.ToggleRun).running shouldBe false }

  it should "toggle running from paused" in {
    val model = Mvu.init(config).copy(running = false)
    Mvu.update(model, Msg.ToggleRun).running shouldBe true
  }

  it should "reset tick on Restart" in {
    val model = Mvu.init(config)
    val afterTick = Mvu.update(model, Msg.Tick)
    Mvu.update(afterTick, Msg.Restart).state.tick shouldBe 0
  }

  it should "preserve running state on Restart" in {
    val model = Mvu.init(config)
    Mvu.update(model, Msg.Restart).running shouldBe true
  }
