package gui

import domain.Shape
import engine.SimulationConfig

import java.awt.{BorderLayout, Dimension}
import javax.swing.{JButton, JFrame, JPanel, Timer, WindowConstants}
import java.awt.event.{ActionEvent, WindowAdapter, WindowEvent}

object SimulationWindow:

  private val timerDelayMs = 30

  def open[S](title: String, config: SimulationConfig[S])(using Renderable[S]): Unit =
    var model = Mvu.init(config)
    val simulationPanel = new SimulationPanel[S]
    val toggleButton = new JButton("Stop")
    val restartButton = new JButton("Restart")
    val controlPanel = new JPanel
    val frame = new JFrame(title)

    def refreshView(): Unit =
      simulationPanel.render(model)
      toggleButton.setText(if model.running then "Stop" else "Resume")

    def dispatch(msg: Msg): Unit =
      model = Mvu.update(model, msg)
      refreshView()

    val timer = new Timer(timerDelayMs, (_: ActionEvent) => dispatch(Msg.Tick))

    toggleButton.addActionListener(_ => dispatch(Msg.ToggleRun))
    restartButton.addActionListener(_ => dispatch(Msg.Restart))

    val dim = config.initialEnvironment.space.shape match
      case Shape.Rectangle(_, width, height) => new Dimension(width.toInt, height.toInt)
      case Shape.Circle(_, radius)           => new Dimension((radius * 2).toInt, (radius * 2).toInt)

    simulationPanel.setPreferredSize(dim)
    frame.setLayout(new BorderLayout)
    frame.add(simulationPanel, BorderLayout.CENTER)
    frame.add(controlPanel, BorderLayout.SOUTH)
    frame.pack()
    frame.setLocationByPlatform(true)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.addWindowListener(
      new WindowAdapter:
        override def windowClosed(e: WindowEvent): Unit = timer.stop()
    )
    frame.setVisible(true)
    refreshView()
    timer.start()
