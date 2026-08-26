import gui.{MainMenu, SimulationOption, SimulationWindow}
import simulations.{AntColony, Epidemic, OpinionDynamics, AlarmSpreading}
import simulations.AntColony.given
import simulations.Epidemic.given
import simulations.OpinionDynamics.given
import simulations.AlarmSpreading.given

/** The entry point of the application. It assembles the catalogue of the available scenarios, pairing the name shown to
  * the user with the action opening the corresponding [[gui.SimulationWindow]], and hands the catalogue over to the
  * main menu, where the choice actually happens.
  */
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
      def name: String = "Sim 4 - Alarm Spreading"
      def start(onBack: () => Unit): Unit = SimulationWindow.open("Alarm Spreading", AlarmSpreading.config, onBack)
  )
  MainMenu.open(options)
