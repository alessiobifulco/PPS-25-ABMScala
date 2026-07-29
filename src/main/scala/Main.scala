import gui.{MainMenu, SimulationOption, SimulationWindow}
import simulations.Epidemic
import simulations.Epidemic.given
import simulations.OpinionDynamics
import simulations.OpinionDynamics.given

@main
def run(): Unit =
  val options = List(
    new SimulationOption:
      def name: String = "Sim 1 - Epidemia"
      def start(): Unit = SimulationWindow.open("Epidemia", Epidemic.config)
    ,
    new SimulationOption:
      def name: String = "Sim 2 - Opinion Dynamics"
      def start(): Unit = SimulationWindow.open("Opinion Dynamics", OpinionDynamics.config)
  )
  MainMenu.open(options)
