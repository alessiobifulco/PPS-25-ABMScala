import domain.{Action, ActionGraph, Move, V2d}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActionGraphTest extends AnyFlatSpec with Matchers:

  "Leaf" should "resolve to the actions produced by its function" in:
    val leaf = ActionGraph.Leaf[String, Int](n => List(Move(V2d(n, n))))
    leaf.resolve(5) shouldBe List(Move(V2d(5, 5)))

  "Branch" should "resolve to ifTrue's actions when the condition holds" in:
    val g = ActionGraph.Branch[String, Int](
      _ > 0,
      ActionGraph.Leaf(_ => List(Move(V2d(1, 0)))),
      ActionGraph.Leaf(_ => List(Move(V2d(-1, 0))))
    )
    g.resolve(5) shouldBe List(Move(V2d(1, 0)))

  it should "resolve to ifFalse's actions when the condition does not hold" in:
    val g = ActionGraph.Branch[String, Int](
      _ > 0,
      ActionGraph.Leaf(_ => List(Move(V2d(1, 0)))),
      ActionGraph.Leaf(_ => List(Move(V2d(-1, 0))))
    )
    g.resolve(-5) shouldBe List(Move(V2d(-1, 0)))

  it should "support nested branches" in:
    val g = ActionGraph.Branch[String, Int](
      _ > 10,
      ActionGraph.Leaf(_ => List(Move(V2d(2, 2)))),
      ActionGraph
        .Branch(_ > 0, ActionGraph.Leaf(_ => List(Move(V2d(1, 1)))), ActionGraph.Leaf(_ => List(Move(V2d(0, 0)))))
    )
    g.resolve(15) shouldBe List(Move(V2d(2, 2)))
    g.resolve(5) shouldBe List(Move(V2d(1, 1)))
    g.resolve(-1) shouldBe List(Move(V2d(0, 0)))
