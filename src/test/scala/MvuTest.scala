import domain.*
import engine.*
import gui.*
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MvuTest extends AnyFlatSpec, Matchers:

  private val mockEnvironment = mock(classOf[Environment[String]])
  private val mockConfig = mock(classOf[SimulationConfig[String]])

  when(mockEnvironment.agents).thenReturn(List.empty)
  when(mockEnvironment.pois).thenReturn(List.empty)
  when(mockConfig.initialEnvironment).thenReturn(mockEnvironment)

  "Mvu.init" should "produce a running model" in:
    Mvu.init(mockConfig).running shouldBe true

  "Mvu.update" should "not advance tick when paused" in:
    val model = Mvu.init(mockConfig).copy(running = false)
    val (newModel, _) = Mvu.update(Msg.Tick).apply(model)
    newModel.state.tick shouldBe 0

  it should "preserve the initial environment" in:
    Mvu.init(mockConfig).state.environment shouldBe mockEnvironment

  it should "advance tick when running" in:
    val model = Mvu.init(mockConfig)
    val (newModel, _) = Mvu.update(Msg.Tick).apply(model)
    newModel.state.tick shouldBe 1

  it should "toggle running from running" in:
    val model = Mvu.init(mockConfig)
    val (newModel, _) = Mvu.update(Msg.ToggleRun).apply(model)
    newModel.running shouldBe false

  it should "toggle running from paused" in:
    val model = Mvu.init(mockConfig).copy(running = false)
    val (newModel, _) = Mvu.update(Msg.ToggleRun).apply(model)
    newModel.running shouldBe true

  it should "reset tick on RestartAndRun" in:
    val model = Mvu.init(mockConfig)
    val (afterTick, _) = Mvu.update(Msg.Tick).apply(model)
    val (restarted, _) = Mvu.update(Msg.RestartAndRun).apply(afterTick)
    restarted.state.tick shouldBe 0

  it should "start running after RestartAndRun" in:
    val model = Mvu.init(mockConfig).copy(running = false)
    val (restarted, _) = Mvu.update(Msg.RestartAndRun).apply(model)
    restarted.running shouldBe true
