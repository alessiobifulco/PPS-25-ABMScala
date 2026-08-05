package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.*
import dsl.BehaviorDsl.*
import dsl.RulesDSL.*

import java.awt.Color

object Epidemic:

  enum Health:
    case Healthy, Infected

  import Health.*

  private val space = RectangularSpace(800, 600)
  private val speed = 2.0
  private val perceptionRadius = 15.0
  private val recoveryChance = 0.002

  private val contagion = whenNear(Healthy, Infected, 1, Infected)
  private val recovery = byChance(Infected, recoveryChance, Healthy)

  val config: SimulationConfig[Health] = SimulationBuilder[Health]().space(space, BoundaryPolicy.bounce)
    .perception(perceptionRadius).population(200, i => if i == 0 then Infected else Healthy)
    .choice(Choice(ctx => ctx.focus.state == Infected, moveHorizontally(speed)))
    .choice(Choice(ctx => true, moveRandomly(speed))).rule(contagion).rule(recovery).build()

  given Renderable[Health] with
    def colorOf(state: Health): Color = state match
      case Healthy  => Color.GREEN
      case Infected => Color.RED
