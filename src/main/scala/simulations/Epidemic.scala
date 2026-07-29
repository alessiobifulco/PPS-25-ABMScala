package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable

import java.awt.Color

object Epidemic:

  enum Health:
    case Healthy, Infected

  import Health.*

  private val space = RectangularSpace(800, 600)
  private val population = 200
  private val speed = 2.0
  private val perceptionRadius = 15.0
  private val recoveryChance = 0.002

  private var list: List[Agent[Health]] = List()

  for i <- 0 until population do
    val state = if i == 0 then Infected else Healthy
    val velocity = if state == Infected then V2d(speed, 0) else V2d.random() * speed
    val agent = Agent(AgentId(i), space.randomPosition, velocity, state)
    list = list :+ agent

  private val agents = list

  private val behavior: Behavior[Health] = Behavior.fromDecision:
    Decision(List(
      Choice(
        ctx => ctx.focus.state == Infected,
        ctx =>
          val x = if ctx.focus.velocity.x < 0 then -speed else speed
          List(Move(V2d(x, 0)))
      ),
      Choice(
        ctx => true,
        ctx =>
          val v = ctx.focus.velocity
          val dir = if v.length > 0 && math.random() > 0.05 then v.normalized else V2d.random()
          List(Move(dir * speed))
      )
    ))

  private val contagion: InteractionRule[Health] = InteractionRule: ctx =>
    if ctx.focus.state == Healthy && ctx.neighbors.exists(_.state == Infected) then Some(Infected) else None

  private val recovery: InteractionRule[Health] = InteractionRule: ctx =>
    if ctx.focus.state == Infected && math.random() < recoveryChance then Some(Healthy) else None

  val config: SimulationConfig[Health] = SimulationConfig(
    Environment(space, agents, BoundaryPolicy.bounce),
    behavior,
    perceptionRadius,
    InteractionRule.firstOf(contagion, recovery)
  )

  given Renderable[Health] with
    def colorOf(state: Health): Color = state match
      case Healthy  => Color.GREEN
      case Infected => Color.RED
