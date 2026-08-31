import gui.Renderable
import java.awt.Color
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RenderableTest extends AnyFlatSpec, Matchers:

  enum TestState:
    case A, B

  private val renderable = new Renderable[TestState]:
    override def colorOf(state: TestState): Color = state match
      case TestState.A => Color.RED
      case TestState.B => Color.BLUE

  "Renderable" should "return the correct color for state A" in:
    renderable.colorOf(TestState.A) shouldBe Color.RED

  it should "return the correct color for state B" in:
    renderable.colorOf(TestState.B) shouldBe Color.BLUE

  it should "return the default toString label" in:
    renderable.labelOf(TestState.A) shouldBe "A"

  it should "return the default toString label for state B" in:
    renderable.labelOf(TestState.B) shouldBe "B"
