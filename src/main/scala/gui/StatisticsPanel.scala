package gui

import domain.AgentId

import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.{BorderFactory, JButton, JLabel, JPanel}

final class StatisticsPanel[S](using renderable: Renderable[S]) extends JPanel:

  private val tickLabel = new JLabel("Tick: -")
  private val agentsLabel = new JLabel("Agents: -")
  private val chartPanel = new ChartPanel
  private val densityPanel = new DensityPanel
  private var history: List[Map[String, Double]] = Nil
  private var labelColors: Map[String, Color] = Map.empty
  private var running = true
  private val toggleStatsButton = new JButton("Stop Stats")
  toggleStatsButton.addActionListener: _ =>
    running = !running
    toggleStatsButton.setText(if running then "Stop Stats" else "Resume Stats")
  private var stateTransitions: Int = 0
  private var previousLabels: Map[AgentId, String] = Map.empty
  private val transitionsLabel = new JLabel("Transitions: -")
  private val poisLabel = new JLabel("POIs: -")
  private var agentGrid: Vector[Vector[Int]] = Vector.fill(StatisticsPanel.GridSize, StatisticsPanel.GridSize)(0)
  private var spaceWidth: Double = 1.0
  private var spaceHeight: Double = 1.0

  setPreferredSize(new Dimension(StatisticsPanel.PanelWidth, 0))
  setLayout(new BorderLayout)
  setBorder(BorderFactory.createEmptyBorder(
    StatisticsPanel.Padding,
    StatisticsPanel.Padding,
    StatisticsPanel.Padding,
    StatisticsPanel.Padding
  ))

  tickLabel.setFont(tickLabel.getFont.deriveFont(Font.BOLD, StatisticsPanel.FontSize))
  agentsLabel.setFont(agentsLabel.getFont.deriveFont(Font.BOLD, StatisticsPanel.FontSize))
  transitionsLabel.setFont(transitionsLabel.getFont.deriveFont(Font.BOLD, StatisticsPanel.FontSize))
  poisLabel.setFont(poisLabel.getFont.deriveFont(Font.BOLD, StatisticsPanel.FontSize))

  private val infoPanel = new JPanel():
    setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS))
    add(tickLabel)
    add(agentsLabel)
    add(transitionsLabel)
    add(poisLabel)

  private val centerPanel = new JPanel(new BorderLayout):
    add(chartPanel, BorderLayout.CENTER)
    add(densityPanel, BorderLayout.SOUTH)

  add(infoPanel, BorderLayout.NORTH)
  add(centerPanel, BorderLayout.CENTER)
  add(toggleStatsButton, BorderLayout.SOUTH)

  def update(model: Model[S]): Unit =
    if !running then return
    val agents = model.state.environment.agents
    val total = agents.size.max(1)
    tickLabel.setText(s"Tick: ${model.state.tick}")
    agentsLabel.setText(s"Agents: ${agents.size}")

    val snapshot = agents.groupBy(agent => renderable.labelOf(agent.state))
      .map((label, group) => label -> group.size.toDouble / total * 100)
    labelColors = labelColors ++ snapshot.keys.filterNot(labelColors.contains)
      .map(label => label -> renderable.colorOf(agents.find(a => renderable.labelOf(a.state) == label).get.state))
    val allLabels = labelColors.keys.toList
    val completeSnapshot = allLabels
      .foldLeft(snapshot)((acc, label) => if acc.contains(label) then acc else acc + (label -> 0.0))
    history = (history :+ completeSnapshot).takeRight(StatisticsPanel.MaxHistory)

    val currentLabels = agents.map(agent => agent.id -> renderable.labelOf(agent.state)).toMap
    val transitions = currentLabels.count((id, label) => previousLabels.get(id).exists(_ != label))
    stateTransitions += transitions
    previousLabels = currentLabels
    transitionsLabel.setText(s"Transitions: $stateTransitions")

    if model.state.environment.pois.isEmpty then poisLabel.setVisible(false)
    else
      poisLabel.setVisible(true)
      val poisStats = model.state.environment.pois.map: poi =>
        val count = agents.count(agent => poi.contains(agent.position))
        s"${poi.name}: $count"
      poisLabel.setText(s"<html>${poisStats.mkString("<br>")}</html>")

    val shape = model.state.environment.space.shape
    val (width, height) = shape match
      case domain.Shape.Rectangle(_, w, h) => (w, h)
      case domain.Shape.Circle(_, r)       => (r * 2, r * 2)
    spaceWidth = width
    spaceHeight = height

    agentGrid = agents.foldLeft(Vector.fill(StatisticsPanel.GridSize, StatisticsPanel.GridSize)(0)): (grid, agent) =>
      val cellColumn = ((agent.position.x / spaceWidth) * StatisticsPanel.GridSize).toInt
        .min(StatisticsPanel.GridSize - 1).max(0)
      val cellRow = ((agent.position.y / spaceHeight) * StatisticsPanel.GridSize).toInt
        .min(StatisticsPanel.GridSize - 1).max(0)
      grid.updated(cellRow, grid(cellRow).updated(cellColumn, grid(cellRow)(cellColumn) + 1))

    chartPanel.repaint()
    densityPanel.repaint()

  private def drawLegendItem(
      graphics2D: Graphics2D,
      color: Color,
      label: String,
      itemX: Int,
      itemY: Int,
      boxSize: Int,
      textOffset: Int
  ): Unit =
    graphics2D.setColor(color)
    graphics2D.fillRect(itemX, itemY, boxSize, boxSize)
    graphics2D.setColor(Color.BLACK)
    graphics2D.drawString(label, itemX + boxSize + textOffset, itemY + boxSize)

  private class ChartPanel extends JPanel:

    override protected def paintComponent(graphics: Graphics): Unit =
      super.paintComponent(graphics)
      if history.isEmpty then return

      val graphics2D = graphics.asInstanceOf[Graphics2D]
      graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      val groups = labelColors.toList
      val legendHeight = groups.size * StatisticsPanel.LegendItemHeight
      val chartWidth = getWidth - StatisticsPanel.Padding * 2 - StatisticsPanel.YLabelWidth
      val chartHeight = getHeight - StatisticsPanel.Padding * 2 - StatisticsPanel.XLabelHeight - legendHeight
      val chartX = StatisticsPanel.Padding + StatisticsPanel.YLabelWidth
      val chartY = StatisticsPanel.Padding
      val totalTicks = history.size.max(2)

      drawAxes(graphics2D, chartX, chartY, chartWidth, chartHeight)
      drawLines(graphics2D, chartX, chartY, chartWidth, chartHeight, totalTicks, groups)
      drawAxisLabels(graphics2D, chartX, chartY, chartWidth, chartHeight)
      drawLegend(graphics2D, chartX, chartY + chartHeight + StatisticsPanel.XLabelHeight, groups)

    private def drawAxes(graphics2D: Graphics2D, chartX: Int, chartY: Int, chartWidth: Int, chartHeight: Int): Unit =
      graphics2D.setColor(Color.LIGHT_GRAY)
      graphics2D.drawRect(chartX, chartY, chartWidth, chartHeight)
      graphics2D.setColor(Color.GRAY)
      graphics2D.setFont(graphics2D.getFont.deriveFont(Font.PLAIN, StatisticsPanel.AxisFontSize))
      for i <- 0 to StatisticsPanel.YTicks do
        val percentage = i * 100 / StatisticsPanel.YTicks
        val y = chartY + chartHeight - (percentage.toDouble / 100 * chartHeight).toInt
        graphics2D.drawLine(chartX - StatisticsPanel.TickSize, y, chartX, y)
        graphics2D.drawString(
          s"$percentage%",
          StatisticsPanel.Padding - StatisticsPanel.YLabelOffset,
          y + StatisticsPanel.AxisFontSize.toInt / 2
        )

    private def drawLines(
        graphics2D: Graphics2D,
        chartX: Int,
        chartY: Int,
        chartWidth: Int,
        chartHeight: Int,
        totalTicks: Int,
        groups: List[(String, Color)]
    ): Unit =
      graphics2D.setStroke(new BasicStroke(StatisticsPanel.LineStroke))
      groups.foreach: (label, color) =>
        graphics2D.setColor(color)
        val points = history.zipWithIndex.map: (snapshot, tick) =>
          val percentage = snapshot.getOrElse(label, 0.0)
          val pointX = chartX + (tick.toDouble / (totalTicks - 1) * chartWidth).toInt
          val pointY = chartY + chartHeight - (percentage / 100.0 * chartHeight).toInt
          (pointX, pointY)
        points.sliding(2).foreach:
          case List((fromX, fromY), (toX, toY)) => graphics2D.drawLine(fromX, fromY, toX, toY)
          case _                                =>

    private def drawAxisLabels(
        graphics2D: Graphics2D,
        chartX: Int,
        chartY: Int,
        chartWidth: Int,
        chartHeight: Int
    ): Unit =
      graphics2D.setColor(Color.DARK_GRAY)
      graphics2D.setFont(graphics2D.getFont.deriveFont(Font.BOLD, StatisticsPanel.AxisFontSize))
      graphics2D.drawString(
        "Time",
        chartX + chartWidth / 2 - StatisticsPanel.TimeOffset,
        chartY + chartHeight + StatisticsPanel.XLabelHeight - StatisticsPanel.Padding / 2
      )

    private def drawLegend(graphics2D: Graphics2D, startX: Int, startY: Int, groups: List[(String, Color)]): Unit =
      graphics2D.setFont(graphics2D.getFont.deriveFont(Font.PLAIN, StatisticsPanel.LegendFontSize))
      groups.indices.foreach: index =>
        val (label, color) = groups(index)
        drawLegendItem(
          graphics2D,
          color,
          label,
          startX,
          startY + index * StatisticsPanel.LegendItemHeight,
          StatisticsPanel.LegendBoxSize,
          StatisticsPanel.Padding / 2
        )

  private class DensityPanel extends JPanel:

    setPreferredSize(new Dimension(0, StatisticsPanel.DensityPanelHeight))

    override protected def paintComponent(graphics: Graphics): Unit =
      super.paintComponent(graphics)
      val graphics2D = graphics.asInstanceOf[Graphics2D]
      graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      val gridSize = StatisticsPanel.GridSize
      val totalAgents = agentGrid.flatten.sum.max(1)
      val availableWidth = getWidth - StatisticsPanel.Padding * 2
      val availableHeight = getHeight - StatisticsPanel.Padding * 2 - StatisticsPanel.DensityLegendHeight
      val cellWidth = availableWidth / gridSize
      val cellHeight = availableHeight / gridSize
      val originX = StatisticsPanel.Padding
      val originY = StatisticsPanel.Padding

      (0 until gridSize).foreach: row =>
        (0 until gridSize).foreach: column =>
          val agentCount = agentGrid(row)(column)
          val densityPercentage = agentCount.toDouble / totalAgents * 100
          val cellColor = densityColor(densityPercentage)
          val cellX = originX + column * cellWidth
          val cellY = originY + row * cellHeight
          graphics2D.setColor(cellColor)
          graphics2D.fillRect(cellX, cellY, cellWidth, cellHeight)
          graphics2D.setColor(Color.LIGHT_GRAY)
          graphics2D.drawRect(cellX, cellY, cellWidth, cellHeight)

      drawDensityLegend(graphics2D, originX, originY + availableHeight + StatisticsPanel.Padding / 2)

    private def densityColor(densityPercentage: Double): Color = densityPercentage match
      case p if p == 0.0  => Color.WHITE
      case p if p <= 8.0  => Color.YELLOW
      case p if p <= 18.0 => Color.ORANGE
      case p if p <= 30.0 => new Color(220, 80, 0)
      case _              => Color.RED

    private def drawDensityLegend(graphics2D: Graphics2D, startX: Int, startY: Int): Unit =
      val legendItems = List(
        (Color.WHITE, "0%"),
        (Color.YELLOW, "1-8%"),
        (Color.ORANGE, "9-18%"),
        (new Color(220, 80, 0), "19-30%"),
        (Color.RED, "31%+")
      )
      graphics2D.setFont(graphics2D.getFont.deriveFont(Font.PLAIN, StatisticsPanel.AxisFontSize))
      val itemWidth = (getWidth - StatisticsPanel.Padding * 2) / legendItems.size
      legendItems.zipWithIndex.foreach:
        case ((color, label), index) =>
          drawLegendItem(graphics2D, color, label, startX + index * itemWidth, startY, StatisticsPanel.LegendBoxSize, 2)

object StatisticsPanel:
  private val PanelWidth = 300
  private val Padding = 10
  private val FontSize = 13f
  private val AxisFontSize = 10f
  private val LegendFontSize = 11f
  private val MaxHistory = 300
  private val LineStroke = 2f
  private val YLabelWidth = 25
  private val XLabelHeight = 20
  private val TimeOffset = 15
  private val YTicks = 4
  private val TickSize = 4
  private val LegendItemHeight = 18
  private val LegendBoxSize = 12
  private val YLabelOffset = 5
  val GridSize = 4
  private val DensityPanelHeight = 140
  private val DensityLegendHeight = 20
