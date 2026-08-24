package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.*
import dsl.Simulation.*

import java.awt.Color

object OpinionDynamics:

  case class Opinion(value: Double)

  private val populationSize = 200
  private val speed = 2.0
  private val perceptionRadius = 60.0
  private val influenceRadius = 20.0
  private val similarityThreshold = 2.0
  private val convergenceRate = 0.02
  private val separationRadius = 15.0
  private val opinionRange = 10.0
  private val spaceCenter = P2d(300, 300)
  private val spaceRadius = 300.0

  given Continuous[Opinion] with
    override def extract(state: Opinion): Double = state.value
    override def update(state: Opinion, value: Double): Opinion = state.copy(value = value)

  private def similar(a: Opinion, b: Opinion): Boolean = math.abs(a.value - b.value) <= similarityThreshold

  private def different(a: Opinion, b: Opinion): Boolean = !similar(a, b)

  val config: SimulationConfig[Opinion] = Simulation.of[Opinion]:
    environment:
      space(CircularSpace(spaceCenter, spaceRadius)) withBoundary wrap
      perception(perceptionRadius)
      population(populationSize) eachBeing Opinion(math.random() * opinionRange)
    behaviour:
      asDefault(follow[Opinion](similar) avoid different movingAt speed keepingApart separationRadius)
    rules:
      convergeTowardsAverage[Opinion] within influenceRadius among similar atRate convergenceRate

  given Renderable[Opinion] with
    override def colorOf(state: Opinion): Color =
      val ratio = (state.value / opinionRange).max(0).min(1)
      Color(ratio.toFloat, 0f, (1 - ratio).toFloat)

    override def labelOf(state: Opinion): String = state.value match
      case v if v < 2.0 => "Very Low"
      case v if v < 4.0 => "Low"
      case v if v < 6.0 => "Medium"
      case v if v < 8.0 => "High"
      case _            => "Very High"
