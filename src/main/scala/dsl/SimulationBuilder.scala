package dsl

import domain.*
import engine.*

import scala.annotation.tailrec

class SimulationBuilder[S]:
  private var space: Space = RectangularSpace(800, 600)
  private var boundary: BoundaryPolicy = BoundaryPolicy.bounce
  private var perception: Double = 10.0
  private var population: Int = 0
  private var states: Option[Int => S] = None
  private var choices: List[Choice[S]] = Nil
  private var rules: List[InteractionRule[S]] = Nil

  def space(space: Space, boundary: BoundaryPolicy): this.type =
    this.space = space
    this.boundary = boundary
    this

  def perception(perception: Double): this.type =
    this.perception = perception
    this

  def population(size: Int, generator: Int => S): this.type =
    this.population = size
    this.states = Some(generator)
    this

  def choice(choice: Choice[S]): this.type =
    this.choices = this.choices :+ choice
    this

  def rule(rule: InteractionRule[S]): this.type =
    this.rules = this.rules :+ rule
    this

  private def generateAgents(size: Int, generator: Int => S): List[Agent[S]] =
    @tailrec
    def _generate(n: Int, acc: List[Agent[S]]): List[Agent[S]] = n match
      case i if i < 0 => acc
      case _          => _generate(n - 1, Agent(AgentId(n), space.randomPosition, V2d.random(), generator(n)) :: acc)
    _generate(size - 1, Nil)

  def build(): SimulationConfig[S] =
    val agents = states match
      case Some(generator) => generateAgents(population, generator)
      case None            => List.empty[Agent[S]]

    SimulationConfig(
      Environment(space, agents, boundary),
      Behavior.fromDecision(Decision(choices)),
      perception,
      InteractionRule.firstOf(rules*)
    )
