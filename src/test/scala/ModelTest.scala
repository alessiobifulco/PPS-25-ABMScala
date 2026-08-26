import domain.*
import engine.*
import gui.*
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ModelTest extends AnyFlatSpec, Matchers:

  private val mockEnvironment = mock(classOf[Environment[String]])
  private val mockConfig = mock(classOf[SimulationConfig[String]])

  when(mockEnvironment.agents).thenReturn(List.empty)
  when(mockConfig.initialEnvironment).thenReturn(mockEnvironment)

  "Model.from" should "create model with tick 0" in:
    Model.from(mockConfig).state.tick shouldBe 0

  it should "start running" in:
    Model.from(mockConfig).running shouldBe true

  it should "store the config" in:
    Model.from(mockConfig).config shouldBe mockConfig

  it should "store the initial environment" in:
    Model.from(mockConfig).state.environment shouldBe mockEnvironment
