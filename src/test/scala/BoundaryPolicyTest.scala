import domain.{BoundaryPolicy, P2d, RectangularSpace, Space, V2d}
import org.mockito.Mockito.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoundaryPolicyTest extends AnyFlatSpec, Matchers:

  private val position = P2d(0.0, 20.0)
  private val velocity = V2d(-2.0, 1.0)

  "BouncePolicy" should "delegate boundary handling to the space" in:
    val space = mock(classOf[Space])
    val expected = (position, velocity)
    when(space.bounce(position, velocity)).thenReturn(expected)
    BoundaryPolicy.bounce(position, velocity, space) shouldBe expected
    verify(space).bounce(position, velocity)

  it should "return the result provided by the space" in:
    val space = mock(classOf[Space])
    val expected = (P2d(100.0, 20.0), V2d(2.0, 1.0))
    when(space.bounce(position, velocity)).thenReturn(expected)
    BoundaryPolicy.bounce(position, velocity, space) shouldBe expected

  "StopPolicy" should "delegate boundary handling to the space" in:
    val space = mock(classOf[Space])
    val expected = (P2d(0.0, 20.0), V2d.zero)
    when(space.stop(position, velocity)).thenReturn(expected)
    BoundaryPolicy.stop(position, velocity, space) shouldBe expected
    verify(space).stop(position, velocity)

  "WrapPolicy" should "wrap positions in a toroidal space" in:
    val space = RectangularSpace(width = 100.0, height = 50.0)
    val outsidePosition = P2d(-10.0, 60.0)
    BoundaryPolicy.wrap(outsidePosition, velocity, space) shouldBe (P2d(90.0, 10.0), velocity)

  it should "fallback to bounce when used with a non-toroidal space" in:
    val space = mock(classOf[Space])
    val expected = (P2d(0.0, 20.0), V2d.zero)
    when(space.bounce(position, velocity)).thenReturn(expected)
    BoundaryPolicy.wrap(position, velocity, space) shouldBe expected
    verify(space).bounce(position, velocity)
