import domain.{BouncePolicy, P2d, RectangularSpace, StopPolicy, V2d, WrapPolicy}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoundaryPolicyTest extends AnyFlatSpec with Matchers:

  private val space = RectangularSpace(width = 100.0, height = 50.0)

  "BouncePolicy" should "delegate boundary handling to the space" in:
    val position = P2d(0.0, 20.0)
    val velocity = V2d(-2.0, 1.0)
    BouncePolicy(position, velocity, space) shouldBe space.bounce(position, velocity)

  it should "preserve position and velocity inside the space" in:
    val position = P2d(20.0, 20.0)
    val velocity = V2d(2.0, 1.0)
    BouncePolicy(position, velocity, space) shouldBe (position, velocity)

  it should "wrap positions to the opposite side" in:
    val position = P2d(-10.0, 60.0)
    val velocity = V2d(-2.0, 1.0)
    WrapPolicy(position, velocity, space) shouldBe (P2d(90.0, 10.0), velocity)

  it should "stop an agent outside the space" in:
    val position = P2d(-10.0, 20.0)
    val velocity = V2d(-2.0, 1.0)
    StopPolicy(position, velocity, space) shouldBe (P2d(0.0, 20.0), V2d.zero)

  it should "preserve an inward velocity at the boundary" in:
    val position = P2d(0.0, 20.0)
    val velocity = V2d(2.0, 1.0)
    StopPolicy(position, velocity, space) shouldBe (position, velocity)
