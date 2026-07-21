import domain.{Bounds, P2d, RectangularSpace, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoundsTest extends AnyFlatSpec with Matchers:

  private val width: Double = 100.0
  private val height: Double = 50.0
  private val bounds = Bounds(width, height)

  "Bounds" should "accept valid dimensions" in:
    bounds.width shouldBe width
    bounds.height shouldBe height

  it should "reject negative or zero dimensions" in:
    an[IllegalArgumentException] should be thrownBy:
      Bounds(-1.0, height)
    an[IllegalArgumentException] should be thrownBy:
      Bounds(width, -1.0)
    an[IllegalArgumentException] should be thrownBy :
      Bounds(0.0, height)
    an[IllegalArgumentException] should be thrownBy :
      Bounds(width, 0.0)

  it should "contain internal positions" in:
    bounds.contains(P2d(20.0, 20.0)) shouldBe true

  it should "contain positions on the boundary" in:
    bounds.contains(P2d(0.0, 20.0)) shouldBe true
    bounds.contains(P2d(width, 20.0)) shouldBe true
    bounds.contains(P2d(20.0, 0.0)) shouldBe true
    bounds.contains(P2d(20.0, height)) shouldBe true

  it should "reject positions outside the bounds" in:
    bounds.contains(P2d(-1.0, 20.0)) shouldBe false
    bounds.contains(P2d(width + 1.0, 20.0)) shouldBe false
    bounds.contains(P2d(20.0, -1.0)) shouldBe false
    bounds.contains(P2d(20.0, height + 1.0)) shouldBe false

  it should "clamp positions outside the bounds" in:
    bounds.clamp(P2d(-10.0, 20.0)) shouldBe P2d(0.0, 20.0)
    bounds.clamp(P2d(110.0, 20.0)) shouldBe P2d(width, 20.0)
    bounds.clamp(P2d(20.0, -10.0)) shouldBe P2d(20.0, 0.0)
    bounds.clamp(P2d(20.0, 60.0)) shouldBe P2d(20.0, height)

  it should "bounce on the horizontal boundaries" in:
    bounds.bounce(P2d(0.0, 20.0), V2d(-2.0, 1.0)) shouldBe (P2d(0.0, 20.0), V2d(2.0, 1.0))

  it should "bounce on the vertical boundaries" in:
    bounds.bounce(P2d(20.0, height), V2d(1.0, 2.0)) shouldBe (P2d(20.0, height), V2d(1.0, -2.0))

  it should "behave like RectangularSpace" in :
    val space = RectangularSpace(width, height)
    val position = P2d(20.0, 20.0)
    val velocity = V2d(-2.0, 1.0)
    val boundsResult = bounds.bounce(position, velocity)
    val spaceResult = space.bounce(position, velocity)
    bounds.contains(position) shouldBe space.contains(position)
    bounds.clamp(position) shouldBe space.clamp(position)
    boundsResult shouldBe spaceResult
