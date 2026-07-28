package gui

import java.awt.GridLayout
import javax.swing.{JButton, JFrame, JPanel}

trait SimulationOption:
  def name: String
  def start(): Unit

object MainMenu:
  private val windowWidth = 350
  private val windowHeight = 250
  private val menuGap = 10

  def open(options: List[SimulationOption]): Unit =
    val frame = JFrame("ABMScala")
    val panel = new JPanel(new GridLayout(options.size.max(1), 1, menuGap, menuGap))

    options.foreach: option =>
      val button = new JButton(option.name)
      button.addActionListener(_ => option.start())
      panel.add(button)

    frame.add(panel)
    frame.setSize(windowWidth, windowHeight)
    frame.setLocationByPlatform(true)
    frame.setVisible(true)
