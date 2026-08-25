package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.*
import dsl.Simulation.*

import java.awt.Color

/** A simulation scenario modeling the spread of a pathogen through a mobile population, inspired by the classic SIR
  * (Susceptible, Infected, Recovered) compartmental model and extended with mortality and the loss of the acquired
  * immunity, which lets recovered individuals become susceptible again.
  */
object Epidemic:

  /** The epidemiological state of an individual agent.
    */
  enum Health:
    case Healthy, Infected, Recovered, Dead

  import Health.*

  private val populationSize = 250
  private val width = 800
  private val height = 600
  private val perceptionRadius = 18.0
  private val healthySpeed = 1.0
  private val infectedSpeed = 3.5
  private val recoveredSpeed = 0.5
  private val transmissionChance = 0.06
  private val mortalityChance = 0.002
  private val recoveryChance = 0.001
  private val immunityLoss = 0.03
  private val decayChance = 0.02

  /** The declarative blueprint of the simulation using the DSL.
    */
  val config: SimulationConfig[Health] = Simulation.of[Health]:
    environment:
      space(RectangularSpace(width, height)) withBoundary bounce
      perception(perceptionRadius)
      population(populationSize) of Healthy withOne Infected
    behavior:
      stopMoving[Health] vanishingWith chance(decayChance) whenAgentIs Dead
      moveRandomly[Health](infectedSpeed) whenAgentIs Infected
      moveRandomly[Health](recoveredSpeed) whenAgentIs Recovered
      asDefault(moveRandomly[Health](healthySpeed))
    rules:
      Infected whenAgentIs Healthy iff atLeastNear(1, Infected)
      Dead whenAgentIs Infected iff chanceOf(mortalityChance)
      Recovered whenAgentIs Infected iff chanceOf(recoveryChance)
      Healthy whenAgentIs Recovered iff chanceOf(immunityLoss)

  given Renderable[Health] with
    override def colorOf(state: Health): Color = state match
      case Healthy   => Color.GREEN
      case Infected  => Color.RED
      case Recovered => Color.YELLOW
      case Dead      => Color.DARK_GRAY
