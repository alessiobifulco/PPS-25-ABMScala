import domain.{CircularSpace, P2d, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CircularSpaceTest extends AnyFlatSpec with Matchers:

  private val center = P2d(0.0, 0.0)
  private val radius = 10.0
  private val space = CircularSpace(center, radius)

  "CircularSpace" should "accept a positive radius" in:
    space.radius shouldBe radius

  it should "reject a non-positive radius" in:
    an[IllegalArgumentException] should be thrownBy:
      CircularSpace(center, 0.0)
    an[IllegalArgumentException] should be thrownBy:
      CircularSpace(center, -1.0)

  it should "contain the center" in:
    space.contains(center) shouldBe true

  it should "contain an internal position" in:
    space.contains(P2d(5.0, 5.0)) shouldBe true

  it should "contain a position on the boundary" in:
    space.contains(P2d(radius, 0.0)) shouldBe true

  it should "reject a position outside the circle" in:
    space.contains(P2d(radius + 1.0, 0.0)) shouldBe false

  it should "leave an internal position unchanged when clamping" in:
    val position = P2d(5.0, 5.0)
    space.clamp(position) shouldBe position

  it should "clamp an external position to the circle boundary" in:
    space.clamp(P2d(20.0, 0.0)) shouldBe P2d(radius, 0.0)

  it should "clamp an external diagonal position to the circle boundary" in:
    val position = P2d(20.0, 20.0)
    val clamped = space.clamp(position)
    space.contains(clamped) shouldBe true
    clamped.x shouldBe (radius / math.sqrt(2))
    clamped.y shouldBe (radius / math.sqrt(2))

  it should "bounce on the right boundary" in:
    val position = P2d(radius, 0.0)
    val velocity = V2d(2.0, 1.0)
    space.bounce(position, velocity) shouldBe (P2d(radius, 0.0), V2d(-2.0, 1.0))

  it should "bounce on the top boundary" in:
    val position = P2d(0.0, radius)
    val velocity = V2d(1.0, 2.0)
    space.bounce(position, velocity) shouldBe (P2d(0.0, radius), V2d(1.0, -2.0))

  it should "stop an external position" in:
    val position = P2d(20.0, 0.0)
    val velocity = V2d(2.0, 1.0)
    space.stop(position, velocity) shouldBe (P2d(radius, 0.0), V2d.zero)

  it should "stop an agent moving outwards on the boundary" in:
    val position = P2d(radius, 0.0)
    val velocity = V2d(2.0, 1.0)
    space.stop(position, velocity) shouldBe (position, V2d.zero)

  it should "preserve an inward velocity on the boundary" in:
    val position = P2d(radius, 0.0)
    val velocity = V2d(-2.0, 1.0)
    space.stop(position, velocity) shouldBe (position, velocity)

  it should "not support wrapping" in:
    an[UnsupportedOperationException] should be thrownBy:
      space.wrap(P2d(20.0, 0.0))
