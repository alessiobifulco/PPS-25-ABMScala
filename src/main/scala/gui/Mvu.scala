package gui

import engine.*

object Mvu:

  def init[S](config: SimulationConfig[S]): Model[S] = 
    Model.from(config)

  def update[S](model: Model[S], msg: Msg): Model[S] = msg match
    case Msg.Tick if model.running => model.copy(state = SimulationEngine.tick(model.state, model.config))
    case Msg.Tick => model
    case Msg.ToggleRun => model.copy(running = !model.running)
    case Msg.Restart => model.copy(state = SimulationEngine.init(model.config))
