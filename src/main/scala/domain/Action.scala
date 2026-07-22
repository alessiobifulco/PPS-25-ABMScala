package domain

import scala.annotation.tailrec

/** An effect that an agent can produce during a tick, distinct from a mere state change. Concrete cases represent the
  * different kinds of effect an agent's [[Behavior]] can express.
  *
  * @tparam S
  *   the type of state used by agents in the simulation.
  */
trait Action[S]

/** An action that changes the velocity of the agent producing it, determining its movement for the current tick.
  *
  * @param velocity
  *   the new velocity to apply.
  * @tparam S
  *   the type of state used by agents in the simulation.
  */
case class Move[S](velocity: V2d) extends Action[S]

/** An action that applies a transformation to the state of another agent, identified by [[AgentId]] (e.g. contagion,
  * mutual influence).
  *
  * @param targetId
  *   the id of the agent to transform.
  * @param transform
  *   the function applied to the target agent's state.
  * @tparam S
  *   the type of state used by agents in the simulation.
  */
case class Nudge[S](targetId: AgentId, transform: S => S) extends Action[S]

/** An action that transmits an event to another agent, identified by [[AgentId]], to be recorded in its memory.
  *
  * @param targetId
  *   the id of the agent receiving the event.
  * @param event
  *   the event to transmit.
  * @tparam S
  *   the type of state used by agents in the simulation.
  */
case class ShareMemory[S](targetId: AgentId, event: S) extends Action[S]

/** A composite action, combining multiple actions produced by the same agent in the same tick (e.g. moving and
  * communicating at once).
  *
  * @param actions
  *   the actions composing this action.
  * @tparam S
  *   the type of state used by agents in the simulation.
  */
case class MultiAction[S](actions: List[Action[S]]) extends Action[S]

/** Utilities for [[Action]].
  */
object Action:

  /** Recursively flattens a list of actions, inlining the content of any [[MultiAction]] so that the result never
    * contains one, regardless of nesting depth. The relative order of actions is preserved.
    *
    * @param actions
    *   the actions to flatten.
    * @tparam S
    *   the type of state used by agents in the simulation.
    * @return
    *   the flattened list of actions.
    */
  def flatten[S](actions: List[Action[S]]): List[Action[S]] =
    @tailrec
    def _flatten(acc: List[Action[S]], remaining: List[Action[S]]): List[Action[S]] = remaining match
      case Nil                     => acc.reverse
      case MultiAction(inner) :: t => _flatten(acc, inner ++ t)
      case other :: t              => _flatten(other :: acc, t)

    _flatten(Nil, actions)
