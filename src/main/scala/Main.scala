import gui.{MainMenu, SimulationOption, SimulationWindow}
import simulations.Epidemic
import simulations.Epidemic.given
import simulations.OpinionDynamics
import simulations.OpinionDynamics.given

@main
def run(): Unit =
  val options: List[SimulationOption] = List(
    new SimulationOption:
      def name: String = "Sim 1 - Epidemia"
      def start(onBack: () => Unit): Unit = SimulationWindow.open("Epidemia", Epidemic.config, onBack)
    ,
    new SimulationOption:
      def name: String = "Sim 2 - Opinion Dynamics"
      def start(onBack: () => Unit): Unit = SimulationWindow.open("Opinion Dynamics", OpinionDynamics.config, onBack)
  )
  MainMenu.open(options)
