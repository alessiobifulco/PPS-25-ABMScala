package gui

import domain.Shape
import engine.SimulationConfig

import java.awt.{BorderLayout, Dimension, GridBagLayout}
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer
import javax.swing.WindowConstants

object SimulationWindow:
  private val TimerDelayMs = 30

  def open[S](title: String, config: SimulationConfig[S], onBack: () => Unit)(using
      Renderable[S],
      POIRenderable
  ): Unit =
    var model = Mvu.init(config)
    val toggleButton = new JButton("Resume")
    val restartButton = new JButton("Restart")
    val backButton = new JButton("<- Back")
    val controlPanel = new JPanel
    val frame = new JFrame(title)
    val simulationPanel = new SimulationPanel[S]
    val statisticsPanel = new StatisticsPanel[S]
    val innerPanel = new JPanel(new BorderLayout):
      add(simulationPanel, BorderLayout.CENTER)
      add(statisticsPanel, BorderLayout.EAST)

    val centerPanel = new JPanel(new GridBagLayout):
      add(innerPanel)

    def refreshView(): Unit =
      simulationPanel.render(model)
      statisticsPanel.update(model)
      toggleButton.setText(if model.running then "Stop" else "Resume")

    def dispatch(msg: Msg): Unit =
      val (newModel, _) = Mvu.update(msg).apply(model)
      model = newModel
      refreshView()

    val timer = new Timer(TimerDelayMs, (_: ActionEvent) => dispatch(Msg.Tick))

    toggleButton.addActionListener(_ => dispatch(Msg.ToggleRun))
    restartButton.addActionListener(_ => dispatch(Msg.RestartAndRun))
    backButton.addActionListener: _ =>
      timer.stop()
      frame.dispose()
      onBack()
    controlPanel.add(backButton)
    controlPanel.add(toggleButton)
    controlPanel.add(restartButton)

    val dim = model.state.environment.space.shape match
      case Shape.Rectangle(_, w, h) => new Dimension(w.toInt, h.toInt)
      case Shape.Circle(_, r)       => new Dimension((r * 2).toInt, (r * 2).toInt)
    simulationPanel.setPreferredSize(dim)
    frame.setLayout(new BorderLayout)
    frame.add(centerPanel, BorderLayout.CENTER)
    frame.add(controlPanel, BorderLayout.SOUTH)
    frame.pack()
    frame.setLocationByPlatform(true)
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    frame.addWindowListener(
      new WindowAdapter:
        override def windowClosing(e: WindowEvent): Unit =
          timer.stop()
          frame.dispose()
          onBack()
    )
    frame.setVisible(true)
    refreshView()
    timer.start()
