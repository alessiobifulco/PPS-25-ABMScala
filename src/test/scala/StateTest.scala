import gui.State
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StateTest extends AnyFlatSpec, Matchers:

  "State" should "execute with the provided initial state" in:
    val computation = State[Int, String](state => (state + 1, s"state-$state"))

    computation(10) shouldBe (11, "state-10")

  it should "return a value without changing the state with unit" in:
    val computation = summon[gui.Monad[[A] =>> State[Int, A]]].unit("result")

    computation(10) shouldBe (10, "result")

  it should "thread the updated state through flatMap" in:
    val computation = State[Int, Int](state => (state + 1, state))
      .flatMap(value => State(nextState => (nextState * 2, value + 10)))

    computation(3) shouldBe (8, 13)

  it should "transform the result with map" in:
    val computation = State[Int, Int](state => (state + 1, state)).map(_ * 2)

    computation(3) shouldBe (4, 6)
