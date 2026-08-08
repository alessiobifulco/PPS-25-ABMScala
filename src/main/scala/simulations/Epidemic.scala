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
  private val speed = 2.0
  private val mortalityChance = 0.001
  private val recoveryChance = 0.002
  private val decayChance = 0.01

  val config: SimulationConfig[Health] = Simulation.of[Health]:
    space(RectangularSpace(800, 600), BoundaryPolicy.bounce)
    perception(15.0)
    population(populationSize, i => if i == 0 then Infected else Healthy)
    behaviour:
      stopMoving[Health] vanishingWith chance(decayChance) whenAgentIs Dead
      moveHorizontally[Health](speed) whenAgentIs Infected
      asDefault(moveRandomly[Health](speed))
    rules:
      Infected when atLeastNear(1) withState Infected whenAgentIs Healthy
      Dead when chance(mortalityChance) whenAgentIs Infected
      Healthy when chance(recoveryChance) whenAgentIs Infected

  given Renderable[Health] with
    override def colorOf(state: Health): Color = state match
      case Healthy  => Color.GREEN
      case Infected => Color.RED
      case Dead     => Color.DARK_GRAY
