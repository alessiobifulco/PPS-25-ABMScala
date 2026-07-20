import domain.V2d
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class V2dTest extends AnyFlatSpec with Matchers:

  "V2d.+" should "sum the components of two vectors" in:
    (V2d(1, 2) + V2d(3, 4)) shouldBe V2d(4, 6)

  "V2d.*" should "scale both components by a factor" in:
    (V2d(2, 3) * 2.0) shouldBe V2d(4, 6)

  "length" should "compute the euclidean norm" in:
    V2d(3, 4).length shouldBe 5.0

  "V2d.zero" should "be the additive identity" in:
    (V2d(2, 3) + V2d.zero) shouldBe V2d(2, 3)

  it should "have length zero" in:
    V2d.zero.length shouldBe 0.0