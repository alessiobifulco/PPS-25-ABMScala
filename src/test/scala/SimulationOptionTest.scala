import gui.SimulationOption
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimulationOptionTest extends AnyFlatSpec with Matchers:

  "SimulationOption" should "expose name correctly" in {
    val option = new SimulationOption:
      def name = "Test"
      def start(onBack: () => Unit): Unit = ()
    option.name shouldBe "Test"
  }

  it should "execute start" in {
    var started = false
    val option = new SimulationOption:
      def name = "Test"
      def start(onBack: () => Unit): Unit = started = true
    option.start(() => ())
    started shouldBe true
  }
