package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.*
import dsl.RulesDSL.*
import dsl.Simulation.*

import dsl.ConditionalBehaviour.*
import dsl.CompositeBehaviour.*

import java.awt.Color

object OpinionDynamics:

  case class Opinion(value: Double)

  private val populationSize = 200
  private val speed = 2.0
  private val influenceRadius = 20.0
  private val similarityThreshold = 2.0
  private val convergenceRate = 0.02
  private val separationRadius = 15.0

  private def similar(a: Opinion, b: Opinion): Boolean = math.abs(a.value - b.value) <= similarityThreshold

  val config: SimulationConfig[Opinion] = Simulation.of[Opinion]:
    space(RectangularSpace(800, 600), BoundaryPolicy.wrap)
    perception(60.0)
    population(populationSize, _ => Opinion(math.random() * 10))
    behaviour:
      flock[Opinion](speed, separationRadius, similar)
    rule(convergeTowardsAverage[Opinion](influenceRadius, similar, convergenceRate, _.value, Opinion(_)))

  given Renderable[Opinion] with
    def colorOf(state: Opinion): Color =
      val ratio = (state.value / 10.0).max(0).min(1)
      new Color(ratio.toFloat, 0f, (1 - ratio).toFloat)
