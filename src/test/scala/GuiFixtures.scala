import domain.*
import engine.*
import gui.Renderable

import java.awt.Color

trait GuiFixtures:

  enum TestState:
    case A, B

  val space = RectangularSpace(800, 600)
  val agentA = Agent(AgentId(0), P2d(100, 100), V2d.zero, TestState.A)
  val agentB = Agent(AgentId(1), P2d(200, 200), V2d.zero, TestState.B)
  val env = Environment(space, List(agentA, agentB), BoundaryPolicy.bounce)
  val config =
    SimulationConfig(initialEnvironment = env, behavior = Behavior(_ => List(Move(V2d.zero))), perceptionRadius = 50.0)

  given Renderable[TestState] with
    def colorOf(state: TestState): Color = state match
      case TestState.A => Color.RED
      case TestState.B => Color.BLUE
