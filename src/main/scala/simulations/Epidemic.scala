package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.*
import dsl.BehaviorDsl.*
import dsl.RulesDSL.*
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
    choice(Choice((ctx: AgentContext[Health]) => ctx.focus.state == Infected, moveHorizontally[Health](speed)))
    choice(Choice((_: AgentContext[Health]) => true, moveRandomly[Health](speed)))
    rule(whenNear(Healthy, Infected, 1, Infected))
    rule(byChance(Infected, recoveryChance, Healthy))

  given Renderable[Health] with
    def colorOf(state: Health): Color = state match
      case Healthy  => Color.GREEN
      case Infected => Color.RED