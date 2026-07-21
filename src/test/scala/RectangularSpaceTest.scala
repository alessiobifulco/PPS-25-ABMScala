import domain.{RectangularSpace, P2d, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RectangularSpaceTest extends AnyFlatSpec with Matchers:

  private val width: Double = 100.0
  private val height: Double = 50.0
  private val rectangularSpace = RectangularSpace(width, height)

  "RectangularSpace" should "accept valid dimensions" in:
    rectangularSpace.width shouldBe width
    rectangularSpace.height shouldBe height

  it should "reject negative or zero dimensions" in:
    an[IllegalArgumentException] should be thrownBy:
      RectangularSpace(-1.0, height)
    an[IllegalArgumentException] should be thrownBy:
      RectangularSpace(width, -1.0)
    an[IllegalArgumentException] should be thrownBy:
      RectangularSpace(0.0, height)
    an[IllegalArgumentException] should be thrownBy:
      RectangularSpace(width, 0.0)

  it should "contain internal positions" in:
    rectangularSpace.contains(P2d(20.0, 20.0)) shouldBe true

  it should "contain positions on the boundary" in:
    rectangularSpace.contains(P2d(0.0, 20.0)) shouldBe true
    rectangularSpace.contains(P2d(width, 20.0)) shouldBe true
    rectangularSpace.contains(P2d(20.0, 0.0)) shouldBe true
    rectangularSpace.contains(P2d(20.0, height)) shouldBe true

  it should "reject positions outside the bounds" in:
    rectangularSpace.contains(P2d(-1.0, 20.0)) shouldBe false
    rectangularSpace.contains(P2d(width + 1.0, 20.0)) shouldBe false
    rectangularSpace.contains(P2d(20.0, -1.0)) shouldBe false
    rectangularSpace.contains(P2d(20.0, height + 1.0)) shouldBe false

  it should "clamp positions outside the bounds" in:
    rectangularSpace.clamp(P2d(-10.0, 20.0)) shouldBe P2d(0.0, 20.0)
    rectangularSpace.clamp(P2d(110.0, 20.0)) shouldBe P2d(width, 20.0)
    rectangularSpace.clamp(P2d(20.0, -10.0)) shouldBe P2d(20.0, 0.0)
    rectangularSpace.clamp(P2d(20.0, 60.0)) shouldBe P2d(20.0, height)

  it should "bounce on the horizontal boundaries" in:
    rectangularSpace.bounce(P2d(0.0, 20.0), V2d(-2.0, 1.0)) shouldBe (P2d(0.0, 20.0), V2d(2.0, 1.0))

  it should "bounce on the vertical boundaries" in:
    rectangularSpace.bounce(P2d(20.0, height), V2d(1.0, 2.0)) shouldBe (P2d(20.0, height), V2d(1.0, -2.0))
