package engine

import domain.*

case class SimulationState[S](environment: Environment[S], tick: Int, nextId: Int = 0)
