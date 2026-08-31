import domain.{BoundaryPolicy, P2d, POI, PoiId, RectangularSpace}
import dsl.{EnvironmentBuilder, Simulation}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EnvironmentBuilderTest extends AnyFlatSpec, Matchers:

  private val space = RectangularSpace(width = 100.0, height = 50.0)

  "EnvironmentBuilder" should "build an environment specification" in:
    val builder = EnvironmentBuilder[String]
    val poi = POI(PoiId(1), "point", P2d(10.0, 10.0), 5.0)
    builder.setSpace(space, BoundaryPolicy.stop)
    builder.setPerceptionRadius(15.0)
    builder.setPopulation(2, i => s"agent-$i")
    builder.setMemoryCapacity(3)
    builder.addPoi(poi)
    val spec = builder.build()
    spec.space shouldBe space
    spec.boundary shouldBe BoundaryPolicy.stop
    spec.perceptionRadius shouldBe 15.0
    spec.populationSize shouldBe 2
    spec.stateAt(0) shouldBe "agent-0"
    spec.stateAt(1) shouldBe "agent-1"
    spec.poiList shouldBe List(poi)
    spec.memoryCapacity shouldBe Some(3)

  it should "use default perception radius and preserve POI insertion order" in:
    val builder = EnvironmentBuilder[Int]
    val firstPoi = POI(PoiId(1), "first", P2d(10.0, 10.0), 5.0)
    val secondPoi = POI(PoiId(2), "second", P2d(20.0, 20.0), 5.0)
    builder.setSpace(space, BoundaryPolicy.bounce)
    builder.setPopulation(1, _ => 0)
    builder.addPoi(firstPoi)
    builder.addPoi(secondPoi)
    val spec = builder.build()
    spec.perceptionRadius shouldBe 10.0
    spec.poiList shouldBe List(firstPoi, secondPoi)
    spec.memoryCapacity shouldBe None

  it should "reject a build without a space" in:
    val builder = EnvironmentBuilder[String]
    builder.setPopulation(1, _ => "state")
    an[IllegalArgumentException] should be thrownBy builder.build()

  it should "reject a build without a population" in:
    val builder = EnvironmentBuilder[String]
    builder.setSpace(space, BoundaryPolicy.bounce)
    an[IllegalArgumentException] should be thrownBy builder.build()

  it should "reject a non-positive population size" in:
    val builder = EnvironmentBuilder[String]
    builder.setSpace(space, BoundaryPolicy.bounce)
    builder.setPopulation(0, _ => "state")
    an[IllegalArgumentException] should be thrownBy builder.build()

  it should "create a population with one shared state using of" in:
    val builder = EnvironmentBuilder[String]
    val config = EnvironmentBuilder.population(3)(using builder)
    config.of("resting")
    builder.setSpace(space, BoundaryPolicy.bounce)
    val spec = builder.build()
    (0 until 3).map(spec.stateAt) shouldBe Seq("resting", "resting", "resting")

  it should "create a population by evaluating eachBeing for every agent" in:
    val builder = EnvironmentBuilder[Int]
    var nextState = 0
    val config = EnvironmentBuilder.population(3)(using builder)
    config.eachBeing:
      nextState += 1
      nextState
    builder.setSpace(space, BoundaryPolicy.bounce)
    val spec = builder.build()
    (0 until 3).map(spec.stateAt) shouldBe Seq(1, 2, 3)

  it should "replace only the first state with withOne" in:
    val builder = EnvironmentBuilder[String]
    val config = EnvironmentBuilder.population(3)(using builder)
    config.of("default").withOne("special")
    builder.setSpace(space, BoundaryPolicy.bounce)
    val spec = builder.build()
    (0 until 3).map(spec.stateAt) shouldBe Seq("special", "default", "default")

  it should "use the provided state for every agent when withOne is first" in:
    val builder = EnvironmentBuilder[String]
    val config = EnvironmentBuilder.population(2)(using builder)
    config.withOne("initial")
    builder.setSpace(space, BoundaryPolicy.bounce)
    val spec = builder.build()
    (0 until 2).map(spec.stateAt) shouldBe Seq("initial", "initial")

  it should "configure an environment through the DSL helpers" in:
    val builder = EnvironmentBuilder[String]
    given EnvironmentBuilder[String] = builder
    EnvironmentBuilder.space(space).withBoundary(BoundaryPolicy.stop)
    EnvironmentBuilder.population(1).of("state")
    EnvironmentBuilder.perception(12.0)
    EnvironmentBuilder.memory(4)
    EnvironmentBuilder.poi(POI(PoiId(1), "point", P2d(10.0, 10.0), 5.0))
    val spec = builder.build()
    spec.boundary shouldBe BoundaryPolicy.stop
    spec.perceptionRadius shouldBe 12.0
    spec.memoryCapacity shouldBe Some(4)
    spec.poiList.map(_.name) shouldBe List("point")

  it should "configure an environment through Simulation.of" in:
    val config = Simulation.of[String]:
      EnvironmentBuilder.environment:
        EnvironmentBuilder.space(space)
        EnvironmentBuilder.population(1).of("state")
    config.initialEnvironment.space shouldBe space
    config.initialEnvironment.agents should have size 1
