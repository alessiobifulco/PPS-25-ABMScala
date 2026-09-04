package gui

import domain.*
import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

/** Panel responsible for rendering the current state of a simulation.
  *
  * @param renderable
  *   renderer used to determine the color of each agent state.
  * @param poiRenderable
  *   renderer used to determine the color of each point of interest.
  * @tparam S
  *   state type associated with the agents.
  */
final class SimulationPanel[S](using renderable: Renderable[S], poiRenderable: POIRenderable) extends JPanel:

  private var currentModel: Option[Model[S]] = None

  /** Updates the model rendered by the panel and requests a repaint.
    *
    * @param model
    *   simulation model to render.
    */
  def render(model: Model[S]): Unit =
    currentModel = Some(model)
    repaint()

  /** Paints the current simulation model on the panel.
    *
    * @param graphics
    *   graphics context used for rendering.
    */
  override protected def paintComponent(graphics: Graphics): Unit =
    super.paintComponent(graphics)
    currentModel.foreach: model =>
      val (environmentWidth, environmentHeight) = model.state.environment.space.shape match
        case Shape.Rectangle(_, width, height) => (width.toInt, height.toInt)
        case Shape.Circle(_, radius)           => ((radius * 2).toInt, (radius * 2).toInt)
      val offsetX = (getWidth - environmentWidth) / 2
      val offsetY = (getHeight - environmentHeight) / 2
      graphics.translate(offsetX, offsetY)
      drawBoundary(graphics)
      drawPOIs(graphics, model)
      drawAgents(graphics, model)
      graphics.translate(-offsetX, -offsetY)

  /** Draws all the points of interest contained in the current environment.
    *
    * @param graphics
    *   graphics context used for rendering.
    * @param model
    *   simulation model containing the points of interest.
    */
  private def drawPOIs(graphics: Graphics, model: Model[S]): Unit = model.state.environment.pois.foreach: poi =>
    val color = poiRenderable.colorOf(poi)
    val positionX = poi.position.x.toInt
    val positionY = poi.position.y.toInt
    val poiRadius = poi.radius.toInt
    graphics.setColor(new Color(color.getRed, color.getGreen, color.getBlue, SimulationPanel.PoiAlpha))
    graphics.fillOval(positionX - poiRadius, positionY - poiRadius, poiRadius * 2, poiRadius * 2)
    graphics.setColor(color)
    graphics.drawOval(positionX - poiRadius, positionY - poiRadius, poiRadius * 2, poiRadius * 2)
    graphics.fillOval(
      positionX - SimulationPanel.PoiCenterRadius,
      positionY - SimulationPanel.PoiCenterRadius,
      SimulationPanel.PoiCenterDiameter,
      SimulationPanel.PoiCenterDiameter
    )

  /** Draws the boundary of the current environment.
    *
    * @param graphics
    *   graphics context used for rendering.
    */
  private def drawBoundary(graphics: Graphics): Unit =
    graphics.setColor(Color.BLACK)
    currentModel.foreach: model =>
      model.state.environment.space.shape match
        case Shape.Rectangle(_, width, height) => graphics.drawRect(0, 0, width.toInt, height.toInt)
        case Shape.Circle(center, radius)      => graphics
            .drawOval((center.x - radius).toInt, (center.y - radius).toInt, (radius * 2).toInt, (radius * 2).toInt)

  /** Draws all the agents contained in the current environment.
    *
    * @param graphics
    *   graphics context used for rendering.
    * @param model
    *   simulation model containing the agents.
    */
  private def drawAgents(graphics: Graphics, model: Model[S]): Unit = model.state.environment.agents.foreach: agent =>
    graphics.setColor(renderable.colorOf(agent.state))
    val positionX = agent.position.x.toInt
    val positionY = agent.position.y.toInt
    graphics.fillOval(
      positionX - SimulationPanel.AgentRadius,
      positionY - SimulationPanel.AgentRadius,
      SimulationPanel.AgentDiameter,
      SimulationPanel.AgentDiameter
    )
    graphics.setColor(Color.BLACK)
    graphics.drawOval(
      positionX - SimulationPanel.AgentRadius,
      positionY - SimulationPanel.AgentRadius,
      SimulationPanel.AgentDiameter,
      SimulationPanel.AgentDiameter
    )

/** Constants used by [[SimulationPanel]] when rendering agents and points of interest.
  */
object SimulationPanel:

  private val AgentRadius = 4
  private val AgentDiameter = AgentRadius * 2
  private val PoiCenterRadius = 4
  private val PoiCenterDiameter = PoiCenterRadius * 2
  private val PoiAlpha = 60
