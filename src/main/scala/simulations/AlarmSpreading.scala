package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.*
import dsl.Simulation.*
import dsl.ConditionalBehavior.to

import java.awt.Color

object AlarmSpreading:

  enum Mood:
    case Calm, Alarmed, Gone

  import Mood.*

  private val populationSize = 250
  private val edge = 270.0
  private val perceptionRadius = 45.0
  private val speed = 0.8
  private val fleeSpeed = 3.0
  private val recall = 4
  private val alarmSpan = 20
  private val calmSpan = 50
  private val startingArea = 60.0
  private val spaceCenter = P2d(300, 300)
  private val spaceRadius = 300.0

  private val start = P2d(540, 440)
  private val danger = POI(PoiId(0), "danger", spaceCenter, 40)

  private def scattered(index: Int): P2d =
    P2d(start.x + (math.random() - 0.5) * startingArea, start.y + (math.random() - 0.5) * startingArea)

  val config: SimulationConfig[Mood] = Simulation.of[Mood]:
    environment:
      space(CircularSpace(spaceCenter, spaceRadius)) withBoundary bounce
      perception(perceptionRadius)
      population(populationSize) of Calm placedAt scattered
      memory(recall)
      poi(danger)
    behavior:
      die[Mood] whenAgentIs Gone
      tellNeighbours[Mood] to moveAwayFrom(danger.position, fleeSpeed) whenAgentIs Alarmed
      moveRandomly[Mood](speed) to rememberSightings(danger) whenAgentIs Calm
    rules:
      Gone whenAgentIs Alarmed iff farFrom(spaceCenter, edge)
      Alarmed whenAgentIs Calm iff recentlySighted(alarmSpan)
      Calm whenAgentIs Alarmed iff nothingSightedIn(calmSpan)

  given Renderable[Mood] with
    override def colorOf(state: Mood): Color = state match
      case Calm    => Color.GREEN
      case Alarmed => Color.RED
      case Gone    => Color.DARK_GRAY
