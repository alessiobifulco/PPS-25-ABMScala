package engine

import domain.{Behavior, Environment, InteractionRule, NeighborStrategy}

case class SimulationConfig[S](
    initialEnvironment: Environment[S],
    behavior: Behavior[S],
    perceptionRadius: Double,
    rule: InteractionRule[S] = InteractionRule.firstOf[S](),
    neighborStrategy: NeighborStrategy[S] = NeighborStrategy.bruteForce[S]
)
