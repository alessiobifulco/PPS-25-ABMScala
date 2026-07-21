import domain.{Action, ActionGraph, Behavior, Move, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BehaviorTest extends AnyFlatSpec with Matchers:

  "Behavior" should "produce the actions returned by its function" in:
    val b = Behavior[String, Int](n => List(Move(V2d(n, n))))
    b(5) shouldBe List(Move(V2d(5, 5)))

  "Behavior.fromGraph" should "resolve the graph on the given context" in:
    val graph = ActionGraph
      .Branch[String, Int](_ > 0, ActionGraph.leaf(Move(V2d(1, 0))), ActionGraph.leaf(Move(V2d(-1, 0))))
    val b = Behavior.fromGraph(graph)

    b(5) shouldBe List(Move(V2d(1, 0)))
    b(-5) shouldBe List(Move(V2d(-1, 0)))

  "andThen" should "combine the actions produced by both behaviors" in:
    val b1 = Behavior[String, Int](n => List(Move(V2d(n, 0))))
    val b2 = Behavior[String, Int](n => List(Move(V2d(0, n))))
    val combined = b1.andThen(b2)

    combined(5) shouldBe List(Move(V2d(5, 0)), Move(V2d(0, 5)))
