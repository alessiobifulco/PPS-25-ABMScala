import gui.{MainMenu, SimulationOption, SimulationWindow}
import simulations.Epidemic
import simulations.Epidemic.given

@main
def run(): Unit =
  val options = List(
    new SimulationOption:
      def name: String = "Sim 1 - Epidemia"
      def start(): Unit = SimulationWindow.open("Epidemia", Epidemic.config)
  )
  MainMenu.open(options)
