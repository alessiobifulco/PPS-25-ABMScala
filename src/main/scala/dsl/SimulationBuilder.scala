package dsl

import domain.*
import engine.SimulationConfig

trait SimulationBuilder[S]:
  def setSpace(space: Space, boundary: BoundaryPolicy): this.type
  def setPerceptionRadius(radius: Double): this.type
  def setPopulationSize(size: Int): this.type
  def setStateGenerator(generator: Int => S): this.type
  def addChoice(choice: Choice[S]): this.type
  def addRule(rule: InteractionRule[S]): this.type
  def build(): SimulationConfig[S]

object SimulationBuilder:

  private[dsl] def apply[S](): SimulationBuilder[S] = SimulationBuilderImpl[S]()

  private class SimulationConfigRecord[S](
      var space: Space = RectangularSpace(800, 600),
      var boundaryPolicy: BoundaryPolicy = BoundaryPolicy.bounce,
      var perceptionRadius: Double = 10.0,
      var populationSize: Int = 0,
      var stateAt: Option[Int => S] = None,
      var choices: List[Choice[S]] = Nil,
      var rules: List[InteractionRule[S]] = Nil
  ):
    def buildConfig(): SimulationConfig[S] =
      val agents = stateAt.fold(List.empty[Agent[S]])(f =>
        List.tabulate(populationSize)(i => Agent(AgentId(i), space.randomPosition, V2d.random(), f(i)))
      )
      SimulationConfig(
        Environment(space, agents, boundaryPolicy),
        Behavior.fromDecision(Decision(choices)),
        perceptionRadius,
        InteractionRule.firstOf(rules*)
      )

  private class SimulationBuilderImpl[S] extends SimulationBuilder[S]:
    private val record = SimulationConfigRecord[S]()

    override def setSpace(space: Space, boundary: BoundaryPolicy): this.type =
      record.space = space
      record.boundaryPolicy = boundary
      this

    override def setPerceptionRadius(radius: Double): this.type =
      record.perceptionRadius = radius
      this

    override def setPopulationSize(size: Int): this.type =
      record.populationSize = size
      this

    override def setStateGenerator(generator: Int => S): this.type =
      record.stateAt = Some(generator)
      this

    override def addChoice(choice: Choice[S]): this.type =
      record.choices = record.choices :+ choice
      this

    override def addRule(rule: InteractionRule[S]): this.type =
      record.rules = record.rules :+ rule
      this

    override def build(): SimulationConfig[S] = record.buildConfig()
