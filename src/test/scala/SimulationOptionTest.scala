import gui.SimulationOption
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimulationOptionTest extends AnyFlatSpec, Matchers:

  "SimulationOption" should "expose its name" in:
    val option = new SimulationOption:
      override def name: String = "Test simulation"
      override def start(onBack: () => Unit): Unit = ()

    option.name shouldBe "Test simulation"

  it should "execute the start operation" in:
    var started = false
    val option = new SimulationOption:
      override def name: String = "Test simulation"
      override def start(onBack: () => Unit): Unit = started = true

    option.start(() => ())
    started shouldBe true

  it should "execute the back callback" in:
    var returned = false
    val option = new SimulationOption:
      override def name: String = "Test simulation"
      override def start(onBack: () => Unit): Unit = onBack()
    option.start(() => returned = true)
    returned shouldBe true
