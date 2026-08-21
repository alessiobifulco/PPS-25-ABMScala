package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable
import dsl.*
import dsl.Simulation.*

import java.awt.Color

object TrustingTrust:

  enum Mood:
    case Trusting, Wary, Bitter

  import Mood.*

  private val populationSize = 150
  private val width = 600
  private val height = 400
  private val perceptionRadius = 35.0
  private val speed = 2.0
  private val recall = 20
  private val gossipChance = 0.05
  private val naiveBetrayal = 0.02
  private val waryBetrayal = 0.10
  private val bitterBetrayal = 0.35
  private val doubtThreshold = 2
  private val bitternessThreshold = 6

  private val oblivion = POI(PoiId(0), P2d(300, 200), 60)

  private def goesWell(other: Mood): Boolean = other match
    case Trusting => !chance(naiveBetrayal).happens
    case Wary     => !chance(waryBetrayal).happens
    case Bitter   => !chance(bitterBetrayal).happens

  private def betrayal(event: MemoryEvent): Boolean = event match
    case MemoryEvent.Encounter(_, positive) => !positive
    case _                                  => false

  private def grudges(ctx: AgentContext[Mood]): Int = ctx.focus.remembers.count(belief => betrayal(belief.event))
  private def resentful(times: Int): AgentContext[Mood] => Boolean = ctx => grudges(ctx) >= times
  private def appeased: AgentContext[Mood] => Boolean = ctx => grudges(ctx) == 0
  private def meeting: ActionSource[Mood] = rememberEncounters[Mood](goesWell)
  private def gossip: ActionSource[Mood] = learnFromNeighbours[Mood](betrayal) onlyIf chanceOf(gossipChance)
  private def amnesia: ActionSource[Mood] = forget[Mood] onlyIf inside(oblivion)
  private def shunning: ActionSource[Mood] = avoidRemembered[Mood](speed, betrayal) orElse moveRandomly(speed)

  val config: SimulationConfig[Mood] = Simulation.of[Mood]:
    environment:
      space(RectangularSpace(width, height)) withBoundary bounce
      perception(perceptionRadius)
      population(populationSize) of Trusting
      memory(recall)
      poi(oblivion)
    behaviour:
      asDefault(meeting to gossip to amnesia to shunning)
    rules:
      Wary when resentful(doubtThreshold) whenAgentIs Trusting
      Bitter when resentful(bitternessThreshold) whenAgentIs Wary
      Trusting when appeased whenAgentIs Wary
      Trusting when appeased whenAgentIs Bitter

  given Renderable[Mood] with
    override def colorOf(state: Mood): Color = state match
      case Trusting => Color.GREEN
      case Wary     => Color.YELLOW
      case Bitter   => Color.RED
