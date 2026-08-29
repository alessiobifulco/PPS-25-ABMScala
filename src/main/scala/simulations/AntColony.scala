package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.*
import dsl.Simulation.*

import java.awt.Color

/** A simulation scenario modeling biological foraging behavior. Agents act as ants shuttling between a central nest and
  * peripheral food sources, utilizing memory to optimize their trips once food is discovered.
  */
object AntColony:

  /** The current occupational role of the ant.
    */
  enum Task:
    case Foraging, Carrying

  import Task.*

  private val populationSize = 150
  private val width = 800
  private val height = 600
  private val perceptionRadius = 60.0
  private val speed = 2.0
  private val recall = 3
  private val harvestDelay = 3

  private val nest = POI(PoiId(0), "Nest", P2d(400, 300), 30)
  private val nearFood = POI(PoiId(1), "Near Food", P2d(120, 120), 25, harvestDelay)
  private val farFood = POI(PoiId(2), "Far Food", P2d(680, 480), 25, harvestDelay)

  /** A composite behavior demonstrating DSL chaining. The ant prioritizes moving towards a remembered food source; if
    * memory is empty, it falls back to wandering randomly.
    */
  private def searchForFood: ActionSource[Task] = moveTowardsRemembered[Task](speed) orElse moveRandomly(speed)

  /** The declarative blueprint of the simulation using the DSL.
    */
  val config: SimulationConfig[Task] = Simulation.of[Task]:
    environment:
      space(RectangularSpace(width, height)) withBoundary bounce
      perception(perceptionRadius)
      population(populationSize) of Foraging
      memory(recall)
      poi(nest)
      poi(nearFood)
      poi(farFood)
    behavior:
      tellNeighbours[Task] to moveTowards(nest.position, speed) whenAgentIs Carrying
      rememberSightings[Task](nearFood, farFood) to searchForFood whenAgentIs Foraging
    rules:
      Carrying whenAgentIs Foraging iff (settledIn(nearFood) or settledIn(farFood))
      Foraging whenAgentIs Carrying iff inside(nest)

  given Renderable[Task] with
    override def colorOf(state: Task): Color = state match
      case Foraging => Color.MAGENTA
      case Carrying => Color.ORANGE
