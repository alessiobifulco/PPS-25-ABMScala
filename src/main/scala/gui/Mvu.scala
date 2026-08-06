package gui

import engine.*

object Mvu:

  def init[S](config: SimulationConfig[S]): Model[S] = Model.from(config)

  private def tick[S]: State[Model[S], Unit] = State: model =>
    if model.running then (model.copy(state = SimulationEngine.tick(model.state, model.config)), ()) else (model, ())

  private def toggleRun[S]: State[Model[S], Unit] = State(model => (model.copy(running = !model.running), ()))

  private def run[S]: State[Model[S], Unit] = State(model => (model.copy(running = true), ()))

  private def restart[S]: State[Model[S], Unit] =
    State(model => (model.copy(state = SimulationEngine.init(model.config)), ()))

  def update[S](msg: Msg): State[Model[S], Unit] = msg match
    case Msg.Tick          => tick
    case Msg.ToggleRun     => toggleRun
    case Msg.RestartAndRun => restart.flatMap(_ => run)
