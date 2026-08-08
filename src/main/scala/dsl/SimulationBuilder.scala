package dsl

import domain.*
import engine.SimulationConfig

trait SimulationBuilder[S]:
  def setSpace(space: Space, boundary: BoundaryPolicy): this.type
  def setPerceptionRadius(radius: Double): this.type
  def setPopulationSize(size: Int): this.type
  def setStateGenerator(generator: Int => S): this.type
  def setActionHandler(handler: ActionHandler[S]): this.type
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
      var stateAt: Option[Int => S] = Option.empty[Int => S],
      var actionHandler: ActionHandler[S] = ActionHandler.default[S],
      var choices: List[Choice[S]] = List.empty[Choice[S]],
      var rules: List[InteractionRule[S]] = List.empty[InteractionRule[S]]
  ):
    def buildConfig(): SimulationConfig[S] =
      assert(stateAt.nonEmpty && populationSize > 0, "Cannot build a simulation without a population!")
      val generator = stateAt.get
      val agents = (0 until populationSize).toList
        .map(i => Agent(AgentId(i), space.randomPosition, V2d.random(), generator(i)))
      SimulationConfig(
        Environment(space, agents, boundaryPolicy),
        Behavior.fromDecision(Decision(choices)),
        perceptionRadius,
        InteractionRule.firstOf(rules*),
        NeighborStrategy.bruteForce[S],
        actionHandler
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

    override def setActionHandler(handler: ActionHandler[S]): this.type =
      record.actionHandler = handler
      this

    override def addChoice(choice: Choice[S]): this.type =
      record.choices = record.choices :+ choice
      this

    override def addRule(rule: InteractionRule[S]): this.type =
      record.rules = record.rules :+ rule
      this

    override def build(): SimulationConfig[S] = record.buildConfig()
