package gui

import engine.*

/** Model-view-update logic used to initialize and update a simulation model.
  */
object Mvu:

  /** Initializes a model from a simulation configuration.
    *
    * @param config
    *   configuration used to initialize the simulation.
    * @tparam S
    *   state type associated with the simulation.
    * @return
    *   the initialized simulation model.
    */
  def init[S](config: SimulationConfig[S]): Model[S] = Model.from(config)

  /** Advances the simulation by one step if it is running.
    *
    * @tparam S
    *   state type associated with the simulation.
    * @return
    *   a state transformation that updates the simulation model.
    */
  private def tick[S]: State[Model[S], Unit] = State: model =>
    if model.running then (model.copy(state = SimulationEngine.tick(model.state, model.config)), ()) else (model, ())

  /** Toggles the running state of the simulation.
    *
    * @tparam S
    *   state type associated with the simulation.
    * @return
    *   a state transformation that updates the running state.
    */
  private def toggleRun[S]: State[Model[S], Unit] = State(model => (model.copy(running = !model.running), ()))

  /** Sets the simulation running state to true.
    *
    * @tparam S
    *   state type associated with the simulation.
    * @return
    *   a state transformation that starts the simulation.
    */
  private def run[S]: State[Model[S], Unit] = State(model => (model.copy(running = true), ()))

  /** Restarts the simulation using its current configuration.
    *
    * @tparam S
    *   state type associated with the simulation.
    * @return
    *   a state transformation that resets the simulation state.
    */
  private def restart[S]: State[Model[S], Unit] =
    State(model => (model.copy(state = SimulationEngine.init(model.config)), ()))

  /** Converts a message into the corresponding model state transformation.
    *
    * @param msg
    *   message that describes the requested operation.
    * @tparam S
    *   state type associated with the simulation.
    * @return
    *   a state transformation corresponding to the input message.
    */
  def update[S](msg: Msg): State[Model[S], Unit] = msg match
    case Msg.Tick          => tick
    case Msg.ToggleRun     => toggleRun
    case Msg.RestartAndRun => restart.flatMap(_ => run)
