package gui

import engine.{SimulationConfig, SimulationEngine, SimulationState}

case class Model[S](state: SimulationState[S], config: SimulationConfig[S], running: Boolean)

object Model:
  def from[S](config: SimulationConfig[S]): Model[S] = Model(SimulationEngine.init(config), config, running = false)
