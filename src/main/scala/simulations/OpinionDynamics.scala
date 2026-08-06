package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.SimulationBuilder
import dsl.BehaviorDsl.*
import dsl.RulesDSL.*

import java.awt.Color

object OpinionDynamics:

  case class Opinion(value: Double)

  private val space = RectangularSpace(800, 600)
  private val populationSize = 200
  private val speed = 2.0
  private val perceptionRadius = 60.0
  private val influenceRadius = 20.0
  private val similarityThreshold = 2.0
  private val convergenceRate = 0.02
  private val separationRadius = 15.0

  private def similar(a: Opinion, b: Opinion): Boolean = math.abs(a.value - b.value) <= similarityThreshold

  private val convergence =
    convergeTowardsAverage[Opinion](influenceRadius, similar, convergenceRate, _.value, Opinion(_))

  val config: SimulationConfig[Opinion] = SimulationBuilder[Opinion]().space(space, BoundaryPolicy.wrap)
    .perception(perceptionRadius).population(populationSize, _ => Opinion(math.random() * 10))
    .choice(Choice(_ => true, flock(speed, separationRadius, similar))).rule(convergence).build()

  given Renderable[Opinion] with
    def colorOf(state: Opinion): Color =
      val ratio = (state.value / 10.0).max(0).min(1)
      new Color(ratio.toFloat, 0f, (1 - ratio).toFloat)
