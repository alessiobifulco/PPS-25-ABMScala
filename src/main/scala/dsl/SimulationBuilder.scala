package dsl

import domain.*
import engine.SimulationConfig

trait SimulationBuilder[S]:
  def setEnvironment(spec: EnvironmentSpec[S]): this.type
  def addBehavior(behavior: Behavior[S]): this.type
  def addRule(rule: InteractionRule[S]): this.type
  def build(): SimulationConfig[S]

object SimulationBuilder:

  private[dsl] def apply[S](): SimulationBuilder[S] = SimulationBuilderImpl[S]()

  private class SimulationBuilderImpl[S] extends SimulationBuilder[S]:

    private var environment: Option[EnvironmentSpec[S]] = None
    private var behaviors: List[Behavior[S]] = Nil
    private var rules: List[InteractionRule[S]] = Nil

    override def setEnvironment(spec: EnvironmentSpec[S]): this.type =
      environment = Some(spec)
      this

    override def addBehavior(behavior: Behavior[S]): this.type =
      behaviors = behaviors :+ behavior
      this

    override def addRule(rule: InteractionRule[S]): this.type =
      rules = rules :+ rule
      this

    override def build(): SimulationConfig[S] = environment match
      case Some(spec) =>
        val memory = spec.memoryCapacity.map(Memory.apply)
        val agents = (0 until spec.populationSize).toList
          .map(i => Agent(AgentId(i), spec.space.randomPosition, V2d.random(), spec.stateAt(i), memory))
        SimulationConfig(
          Environment(spec.space, agents, spec.boundary, spec.poiList),
          behaviors,
          spec.perceptionRadius,
          rules
        )

      case None => throw new IllegalStateException(
        "Cannot build the simulation: environment is missing. Please call setEnvironment() first."
      )