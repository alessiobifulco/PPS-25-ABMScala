package engine

import domain.*

case class SimulationConfig[S](
    initialEnvironment: Environment[S],
    behaviors: List[Behavior[S]],
    perceptionRadius: Double,
    rules: List[InteractionRule[S]] = List.empty,
    neighborStrategy: NeighborStrategy[S] = NeighborStrategy.bruteForce[S]
)
