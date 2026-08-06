package domain

trait POIEffect[S]:
  def apply(agent: Agent[S]): Agent[S]

object POIEffect:
  def apply[S](f: Agent[S] => Agent[S]): POIEffect[S] = f(_)
  def transformState[S](f: S => S): POIEffect[S] = agent => agent.withState(f(agent.state))
  def noop[S]: POIEffect[S] = agent => agent

case class POI[S](id: Int, position: P2d, radius: Double, activationDelay: Int, effect: POIEffect[S]):
  def affects(agent: Agent[S]): Boolean = (agent.position - position).length <= radius

object POI:
  def at[S](id: Int, position: P2d, radius: Double, activationDelay: Int = 0)(effect: S => S): POI[S] =
    POI(id, position, radius, activationDelay, POIEffect.transformState(effect))

case class Residency(perPoi: Map[Int, Int]):
  def tickFor(poiId: Int): Residency = copy(perPoi = perPoi.updatedWith(poiId)(t => Some(t.getOrElse(0) + 1)))
  def reset(poiId: Int): Residency = copy(perPoi = perPoi - poiId)
  def ticksIn(poiId: Int): Int = perPoi.getOrElse(poiId, 0)

object Residency:
  val empty: Residency = Residency(Map.empty)
