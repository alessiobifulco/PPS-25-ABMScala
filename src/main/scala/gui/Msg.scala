package gui

/** Message used to manage the simulation state and execution.
  */
enum Msg:

  /** Advances the simulation by one step.
    */
  case Tick

  /** Toggles the running state of the simulation.
    */
  case ToggleRun

  /** Restarts the simulation and starts its execution.
    */
  case RestartAndRun
