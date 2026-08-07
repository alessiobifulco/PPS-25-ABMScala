package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.*
import dsl.Simulation.*

import java.awt.Color

object Epidemic:

  enum Health:
    case Healthy, Infected

  import Health.*

  private val speed = 2.0
  private val recoveryChance = 0.002

  val config: SimulationConfig[Health] = Simulation.of[Health]:
    space(RectangularSpace(800, 600), BoundaryPolicy.bounce)
    perception(15.0)
    population(200, i => if i == 0 then Infected else Healthy)
    behaviour:
      moveHorizontally[Health](speed) whenAgentIs Infected
      moveRandomly[Health](speed)
    Infected when atLeastNear(1) withState Infected whenAgentIs Healthy
    Healthy when chance(recoveryChance) whenAgentIs Infected

  given Renderable[Health] with
    def colorOf(state: Health): Color = state match
      case Healthy  => Color.GREEN
      case Infected => Color.RED
