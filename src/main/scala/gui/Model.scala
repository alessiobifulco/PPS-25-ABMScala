package gui

import engine.{SimulationConfig, SimulationEngine, SimulationState}

/** Model containing the current state and configuration of a simulation.
  *
  * @param state
  *   current simulation state.
  * @param config
  *   configuration used by the simulation.
  * @param running
  *   indicates whether the simulation is currently running.
  * @tparam S
  *   state type associated with the simulation.
  */
case class Model[S](state: SimulationState[S], config: SimulationConfig[S], running: Boolean)

/** Factory methods for [[Model]].
  */
object Model:

  /** Creates an initial model from a simulation configuration.
    *
    * @param config
    *   configuration used to initialize the simulation.
    * @tparam S
    *   state type associated with the simulation.
    * @return
    *   a model containing the initial simulation state and marked as running.
    */
  def from[S](config: SimulationConfig[S]): Model[S] = Model(SimulationEngine.init(config), config, running = true)
