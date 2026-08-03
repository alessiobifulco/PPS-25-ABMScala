package gui

import engine.*
import State.*
import Monad.*

object Mvu:

  def init[S](config: SimulationConfig[S]): Model[S] = Model.from(config)

  def update[S](msg: Msg): State[Model[S], Unit] = msg match
    case Msg.Tick => State(model =>
        if model.running then (model.copy(state = SimulationEngine.tick(model.state, model.config)), ())
        else (model, ())
      )
    case Msg.ToggleRun => State(model => (model.copy(running = !model.running), ()))
    case Msg.Restart   => State(model => (model.copy(state = SimulationEngine.init(model.config)), ()))
