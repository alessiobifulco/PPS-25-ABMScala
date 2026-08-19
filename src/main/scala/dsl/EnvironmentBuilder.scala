package dsl

import domain.*

private[dsl] case class EnvironmentSpec[S](
    space: Space,
    boundary: BoundaryPolicy,
    perceptionRadius: Double,
    populationSize: Int,
    stateAt: Int => S,
    pois: List[POI],
    memoryCapacity: Option[Int]
)

trait EnvironmentBuilder[S]:
  def setSpace(space: Space, boundary: BoundaryPolicy): Unit
  def setPerceptionRadius(radius: Double): Unit
  def setPopulation(size: Int, generator: Int => S): Unit
  def setMemoryCapacity(capacity: Int): Unit
  def addPoi(poi: POI): Unit
  def build(): EnvironmentSpec[S]

object EnvironmentBuilder:

  private val defaultBoundary = BoundaryPolicy.bounce

  def apply[S](): EnvironmentBuilder[S] = EnvironmentBuilderImpl[S]()

  def environment[S](block: EnvironmentBuilder[S] ?=> Unit)(using simBuilder: SimulationBuilder[S]): Unit =
    val builder = EnvironmentBuilder[S]()
    block(using builder)
    val spec = builder.build()
    simBuilder.setSpace(spec.space, spec.boundary).setPerceptionRadius(spec.perceptionRadius)
      .setPopulationSize(spec.populationSize).setStateGenerator(spec.stateAt)

  def space[S](s: Space)(using builder: EnvironmentBuilder[S]): SpaceConfig[S] =
    builder.setSpace(s, defaultBoundary)
    SpaceConfig(s, builder)

  def population[S](size: Int)(using builder: EnvironmentBuilder[S]): PopulationConfig[S] =
    PopulationConfig(size, builder)

  def perception[S](radius: Double)(using builder: EnvironmentBuilder[S]): Unit = builder.setPerceptionRadius(radius)

  def memory[S](capacity: Int)(using builder: EnvironmentBuilder[S]): Unit = builder.setMemoryCapacity(capacity)

  def poi(p: POI)(using builder: EnvironmentBuilder[?]): Unit = builder.addPoi(p)

  final class SpaceConfig[S](s: Space, builder: EnvironmentBuilder[S]):
    infix def withBoundary(policy: BoundaryPolicy): Unit = builder.setSpace(s, policy)

  final class PopulationConfig[S](size: Int, builder: EnvironmentBuilder[S]):
    private var current: Option[Int => S] = None

    infix def of(state: S): PopulationConfig[S] = generatedBy(_ => state)

    infix def eachBeing(state: => S): PopulationConfig[S] = generatedBy(_ => state)

    infix def withOne(state: S): PopulationConfig[S] = current match
      case Some(previous) => generatedBy(i => if i == 0 then state else previous(i))
      case _              => generatedBy(_ => state)

    private def generatedBy(generator: Int => S): PopulationConfig[S] =
      current = Some(generator)
      builder.setPopulation(size, generator)
      this

  private class EnvironmentBuilderImpl[S] extends EnvironmentBuilder[S]:
    private var space: Space = RectangularSpace(800, 600)
    private var boundary: BoundaryPolicy = defaultBoundary
    private var perceptionRadius: Double = 10.0
    private var populationSize: Int = 0
    private var stateAt: Option[Int => S] = None
    private var poisAcc: List[POI] = Nil
    private var memoryCapacity: Option[Int] = None

    override def setSpace(s: Space, b: BoundaryPolicy): Unit =
      space = s
      boundary = b

    override def setPerceptionRadius(radius: Double): Unit = perceptionRadius = radius

    override def setPopulation(size: Int, generator: Int => S): Unit =
      populationSize = size
      stateAt = Some(generator)

    override def setMemoryCapacity(capacity: Int): Unit = memoryCapacity = Some(capacity)

    override def addPoi(p: POI): Unit = poisAcc = p :: poisAcc

    override def build(): EnvironmentSpec[S] = stateAt match
      case Some(generator) =>
        require(populationSize > 0, "Population size must be positive")
        EnvironmentSpec(space, boundary, perceptionRadius, populationSize, generator, poisAcc.reverse, memoryCapacity)
      case _ => throw IllegalStateException("Cannot build an environment without a population!")
