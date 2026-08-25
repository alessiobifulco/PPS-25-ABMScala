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

/** Window used to display and control a simulation.
  */
object SimulationWindow:

  /** Delay, expressed in milliseconds, between two consecutive simulation updates.
    */
  private val TimerDelayMs = 30

  /** Opens the simulation window and initializes all graphical components required to display, control and monitor the
    * simulation.
    *
    * @param title
    *   title displayed in the simulation window.
    * @param config
    *   configuration used to initialize the simulation.
    * @param onBack
    *   callback invoked when the simulation window is closed.
    * @tparam S
    *   type of the simulation state.
    * @return
    *   Unit.
    */
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

    /** Refreshes the graphical components using the current simulation model.
      */
    def refreshView(): Unit =
      simulationPanel.render(model)
      statisticsPanel.update(model)
      toggleButton.setText(if model.running then "Stop" else "Resume")

    /** Dispatches a message to the MVU architecture and refreshes the view.
      *
      * @param msg
      *   message to dispatch.
      */
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

    val dimension = model.state.environment.space.shape match
      case Shape.Rectangle(_, width, height) => new Dimension(width.toInt, height.toInt)
      case Shape.Circle(_, radius)           => new Dimension((radius * 2).toInt, (radius * 2).toInt)

    simulationPanel.setPreferredSize(dimension)
    frame.setLayout(new BorderLayout)
    frame.add(centerPanel, BorderLayout.CENTER)
    frame.add(controlPanel, BorderLayout.SOUTH)
    frame.pack()
    frame.setLocationByPlatform(true)
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

    frame.addWindowListener(
      new WindowAdapter:
        override def windowClosing(event: WindowEvent): Unit =
          timer.stop()
          frame.dispose()
          onBack()
    )

    frame.setVisible(true)
    refreshView()
    timer.start()
