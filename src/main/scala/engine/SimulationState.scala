package engine

import domain.Environment

case class SimulationState[S](environment: Environment[S], tick: Int)
