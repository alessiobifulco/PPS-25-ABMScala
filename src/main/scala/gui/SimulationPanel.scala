package gui

import java.awt.Graphics
import javax.swing.JPanel

final class SimulationPanel[S](using renderable: Renderable[S]) extends JPanel:

  private var currentModel: Option[Model[S]] = None

  def render(model: Model[S]): Unit =
    currentModel = Some(model)
    repaint()

  override protected def paintComponent(graphics: Graphics): Unit =
    super.paintComponent(graphics)
    currentModel.foreach(drawAgents(graphics, _))

  private def drawAgents(graphics: Graphics, model: Model[S]): Unit = model.state.environment.agents.foreach: agent =>
    graphics.setColor(renderable.colorOf(agent.state))
    val x = agent.position.x.toInt
    val y = agent.position.y.toInt
    graphics.fillOval(
      x - SimulationPanel.AgentRadius,
      y - SimulationPanel.AgentRadius,
      SimulationPanel.AgentDiameter,
      SimulationPanel.AgentDiameter
    )

object SimulationPanel:
  private val AgentRadius = 4
  private val AgentDiameter = AgentRadius * 2
