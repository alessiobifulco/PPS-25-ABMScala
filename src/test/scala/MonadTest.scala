import gui.Monad
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MonadTest extends AnyFlatSpec, Matchers:

  case class Box[A](value: A)

  given Monad[[A] =>> Box[A]] with

    override def unit[A](value: A): Box[A] = Box(value)

    extension [A](box: Box[A]) override def flatMap[B](f: A => Box[B]): Box[B] = f(box.value)

  "Monad" should "wrap a value with unit" in:
    summon[Monad[[A] =>> Box[A]]].unit(10) shouldBe Box(10)

  it should "apply flatMap to the contained value" in:
    val box = Box(10)
    box.flatMap(value => Box(value * 2)) shouldBe Box(20)

  it should "transform a value with the default map implementation" in:
    val box = Box(10)
    box.map(value => value.toString) shouldBe Box("10")
