package gui

import domain.*
import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

final class SimulationPanel[S](pois: List[POI])(using renderable: Renderable[S], poiRenderable: POIRenderable)
    extends JPanel:

  private var currentModel: Option[Model[S]] = None

  def render(model: Model[S]): Unit =
    currentModel = Some(model)
    repaint()

  override protected def paintComponent(graphics: Graphics): Unit =
    super.paintComponent(graphics)
    currentModel.foreach: model =>
      val (envWidth, envHeight) = model.state.environment.space.shape match
        case Shape.Rectangle(_, w, h) => (w.toInt, h.toInt)
        case Shape.Circle(_, r)       => ((r * 2).toInt, (r * 2).toInt)
      val offsetX = (getWidth - envWidth) / 2
      val offsetY = (getHeight - envHeight) / 2
      graphics.translate(offsetX, offsetY)
      drawBoundary(graphics)
      drawPOIs(graphics)
      drawAgents(graphics, model)
      graphics.translate(-offsetX, -offsetY)

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

  private def drawBoundary(graphics: Graphics): Unit =
    graphics.setColor(Color.BLACK)
    currentModel.foreach: model =>
      model.state.environment.space.shape match
        case Shape.Rectangle(_, w, h) => graphics.drawRect(0, 0, w.toInt, h.toInt)
        case Shape.Circle(c, r) => graphics.drawOval((c.x - r).toInt, (c.y - r).toInt, (r * 2).toInt, (r * 2).toInt)

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
