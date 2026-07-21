import domain.{P2d, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class P2dTest extends AnyFlatSpec with Matchers:

  "P2d.+" should "translate a position by a vector" in:
    (P2d(1, 1) + V2d(2, 3)) shouldBe P2d(3, 4)

  it should "leave the position unchanged when adding the zero vector" in:
    (P2d(5, 5) + V2d.zero) shouldBe P2d(5, 5)

  "P2d.-" should "compute the vector difference between two positions" in:
    (P2d(4, 6) - P2d(1, 2)) shouldBe V2d(3, 4)

  it should "produce the zero vector when subtracting a position from itself" in:
    (P2d(3, 3) - P2d(3, 3)) shouldBe V2d.zero