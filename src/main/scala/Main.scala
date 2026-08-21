import gui.{MainMenu, SimulationOption, SimulationWindow}
import simulations.{AntColony, Epidemic, OpinionDynamics, TrustingTrust}
import simulations.AntColony.given
import simulations.Epidemic.given
import simulations.OpinionDynamics.given
import simulations.TrustingTrust.given

@main
def run(): Unit =
  val options: List[SimulationOption] = List(
    new SimulationOption:
      def name: String = "Sim 1 - Epidemic"
      def start(onBack: () => Unit): Unit = SimulationWindow.open("Epidemic", Epidemic.config, onBack)
    ,
    new SimulationOption:
      def name: String = "Sim 2 - Opinion Dynamics"
      def start(onBack: () => Unit): Unit = SimulationWindow.open("Opinion Dynamics", OpinionDynamics.config, onBack)
    ,
    new SimulationOption:
      def name: String = "Sim 3 - Ant Colony"
      def start(onBack: () => Unit): Unit = SimulationWindow.open("Ant Colony", AntColony.config, onBack)
    ,
    new SimulationOption:
      def name: String = "Sim 4 - Trusting Trust"
      def start(onBack: () => Unit): Unit = SimulationWindow.open("Trusting Trust", TrustingTrust.config, onBack)
  )
  MainMenu.open(options)
