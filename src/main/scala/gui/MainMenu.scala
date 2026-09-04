package gui

import java.awt.{BorderLayout, Dimension, Font, GridBagConstraints, GridBagLayout, Insets}
import javax.swing.{BorderFactory, JButton, JFrame, JLabel, JPanel, SwingConstants, WindowConstants}

/** Option available in the main menu for starting a simulation.
  */
trait SimulationOption:

  /** Name displayed for this simulation option.
    *
    * @return
    *   the option name.
    */
  def name: String

  /** Starts the simulation associated with this option.
    *
    * @param onBack
    *   callback used to return to the main menu.
    */
  def start(onBack: () => Unit): Unit

/** Main menu used to select and start a simulation.
  */
object MainMenu:

  private val InitialWidth = 400
  private val InitialHeight = 350
  private val ButtonMaxWidth = 300
  private val ButtonMaxHeight = 45
  private val TitleFontSize = 24f
  private val TitlePadding = 20
  private val ButtonGap = 10

  /** Opens the main menu with the specified simulation options.
    *
    * @param options
    *   simulation options displayed in the menu.
    */
  def open(options: List[SimulationOption]): Unit =
    val frame = new JFrame("ABMScala")

    val titleLabel = new JLabel("ABMScala", SwingConstants.CENTER)
    titleLabel.setFont(titleLabel.getFont.deriveFont(Font.BOLD, TitleFontSize))
    titleLabel.setBorder(BorderFactory.createEmptyBorder(TitlePadding, 0, TitlePadding, 0))

    val buttonPanel = new JPanel(new GridBagLayout)
    val constraints = new GridBagConstraints
    constraints.gridx = 0
    constraints.fill = GridBagConstraints.HORIZONTAL
    constraints.insets = new Insets(ButtonGap / 2, 0, ButtonGap / 2, 0)

    options.zipWithIndex.foreach: (option, i) =>
      val button = new JButton(option.name)
      button.setMaximumSize(new Dimension(ButtonMaxWidth, ButtonMaxHeight))
      button.setPreferredSize(new Dimension(ButtonMaxWidth, ButtonMaxHeight))
      button.addActionListener: _ =>
        frame.setVisible(false)
        option.start(() => frame.setVisible(true))
      constraints.gridy = i
      buttonPanel.add(button, constraints)

    val centerPanel = new JPanel(new GridBagLayout)
    centerPanel.add(buttonPanel)

    frame.setLayout(new BorderLayout)
    frame.add(titleLabel, BorderLayout.NORTH)
    frame.add(centerPanel, BorderLayout.CENTER)
    frame.setSize(InitialWidth, InitialHeight)
    frame.setResizable(true)
    frame.setLocationByPlatform(true)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setVisible(true)
