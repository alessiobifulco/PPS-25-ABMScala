package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.*
import dsl.Simulation.*

import java.awt.Color

object Epidemic:

  enum Health:
    case Healthy, Infected, Dead

  import Health.*

  private val populationSize = 200
  private val width = 800
  private val height = 600
  private val perceptionRadius = 15.0
  private val speed = 2.0
  private val mortalityChance = 0.001
  private val recoveryChance = 0.002
  private val decayChance = 0.01

  val config: SimulationConfig[Health] = Simulation.of[Health]:
    environment:
      space(RectangularSpace(width, height)) withBoundary bounce
      perception(perceptionRadius)
      population(populationSize) of Healthy withOne Infected
    behavior:
      stopMoving[Health] vanishingWith chance(decayChance) whenAgentIs Dead
      moveHorizontally[Health](speed) whenAgentIs Infected
      asDefault(moveRandomly[Health](speed))
    rules:
      Infected whenAgentIs Healthy iff atLeastNear(1, Infected)
      Dead whenAgentIs Infected iff chanceOf(mortalityChance)
      Healthy whenAgentIs Infected iff chanceOf(recoveryChance)

  given Renderable[Health] with
    override def colorOf(state: Health): Color = state match
      case Healthy  => Color.GREEN
      case Infected => Color.RED
      case Dead     => Color.DARK_GRAY
