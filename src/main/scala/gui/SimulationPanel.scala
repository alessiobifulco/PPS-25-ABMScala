package gui

import domain.*
import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

final class SimulationPanel[S](pois: List[POI[S]])(using renderable: Renderable[S], poiRenderable: POIRenderable[S])
    extends JPanel:

  private var currentModel: Option[Model[S]] = None

  def render(model: Model[S]): Unit =
    currentModel = Some(model)
    repaint()

  override protected def paintComponent(graphics: Graphics): Unit =
    super.paintComponent(graphics)
    drawPOIs(graphics)
    currentModel.foreach(drawAgents(graphics, _))

  private def drawPOIs(graphics: Graphics): Unit = pois.foreach: poi =>
    val color = poiRenderable.colorOf(poi)
    val x = poi.position.x.toInt
    val y = poi.position.y.toInt
    val r = poi.radius.toInt
    graphics.setColor(new Color(color.getRed, color.getGreen, color.getBlue, SimulationPanel.PoiAlpha))
    graphics.fillOval(x - r, y - r, r * 2, r * 2)
    graphics.setColor(color)
    graphics.drawOval(x - r, y - r, r * 2, r * 2)
    graphics.fillOval(
      x - SimulationPanel.PoiCenterRadius,
      y - SimulationPanel.PoiCenterRadius,
      SimulationPanel.PoiCenterDiameter,
      SimulationPanel.PoiCenterDiameter
    )

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
  private val PoiCenterRadius = 4
  private val PoiCenterDiameter = PoiCenterRadius * 2
  private val PoiAlpha = 60
