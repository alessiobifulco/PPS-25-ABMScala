package simulations

import domain.*
import engine.SimulationConfig
import gui.Renderable

import java.awt.Color
import scala.annotation.tailrec

object OpinionDynamics:

  case class Opinion(value: Double)

  private val space = RectangularSpace(800, 600)

  private val population = 200
  private val speed = 2.0
  private val perceptionRadius = 60.0
  private val influenceRadius = 20.0
  private val similarityThreshold = 2.0
  private val convergenceRate = 0.02

  private val cohesionWeight = 1.0
  private val alignmentWeight = 1.0
  private val separationWeight = 1.5
  private val inertiaWeight = 0.5
  private val separationRadius = 15.0

  private def similar(a: Opinion, b: Opinion): Boolean = math.abs(a.value - b.value) <= similarityThreshold

  private def createAgents(): List[Agent[Opinion]] = (0 until population).toList.map: i =>
    Agent(AgentId(i), space.randomPosition, V2d.random() * speed, Opinion(math.random() * 10))

  private val agents = createAgents()

  private def centroidOffset(from: P2d, group: List[Agent[Opinion]]): V2d =
    if group.isEmpty then V2d.zero
    else
      val centerX = group.map(_.position.x).sum / group.size
      val centerY = group.map(_.position.y).sum / group.size
      P2d(centerX, centerY) - from

  private def averageHeading(group: List[Agent[Opinion]]): V2d =
    if group.isEmpty then V2d.zero
    else
      val avgX = group.map(_.velocity.x).sum / group.size
      val avgY = group.map(_.velocity.y).sum / group.size
      V2d(avgX, avgY)

  private def separationOffset(from: P2d, repellers: List[Agent[Opinion]]): V2d =
    @tailrec
    def loop(remaining: List[Agent[Opinion]], acc: V2d): V2d = remaining match
      case Nil          => acc
      case head :: tail =>
        val away = from - head.position
        val next = if away.length > 0 then acc + (away * (1.0 / away.length)) else acc
        loop(tail, next)
    loop(repellers, V2d.zero)

  private val behavior: Behavior[Opinion] = Behavior: ctx =>
    val (alike, different) = ctx.neighbors.partition(n => similar(n.state, ctx.focus.state))
    val tooClose = ctx.neighbors.filter(n => (n.position - ctx.focus.position).length < separationRadius)
    val repellers = different ++ tooClose
    val cohesionOffset = centroidOffset(ctx.focus.position, alike)
    val cohesion = if cohesionOffset.length > 0 then cohesionOffset.normalized else V2d.zero
    val heading = averageHeading(alike)
    val alignment = if heading.length > 0 then heading.normalized else V2d.zero
    val separationRaw = separationOffset(ctx.focus.position, repellers)
    val separation = if separationRaw.length > 0 then separationRaw.normalized else V2d.zero
    val inertia = if ctx.focus.velocity.length > 0 then ctx.focus.velocity.normalized else V2d.random()
    val direction = (cohesion * cohesionWeight) + (alignment * alignmentWeight) + (separation * separationWeight) +
      (inertia * inertiaWeight)
    val finalDirection = if direction.length > 0 then direction.normalized else inertia
    List(Move(finalDirection * speed))

  private val convergence: InteractionRule[Opinion] = InteractionRule: ctx =>
    val influencing = ctx.visibleWithin(influenceRadius).filter(n => similar(n.state, ctx.focus.state))
    if influencing.isEmpty then None
    else
      val own = ctx.focus.state.value
      val average = influencing.map(_.state.value).sum / influencing.size
      Some(Opinion(own + (average - own) * convergenceRate))

  val config: SimulationConfig[Opinion] =
    SimulationConfig(Environment(space, agents, BoundaryPolicy.wrap), behavior, perceptionRadius, convergence)

  given Renderable[Opinion] with

    def colorOf(state: Opinion): Color =
      val ratio = (state.value / 10.0).max(0).min(1)
      new Color(ratio.toFloat, 0f, (1 - ratio).toFloat)
