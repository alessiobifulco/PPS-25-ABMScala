package domain

import scala.annotation.tailrec

trait Action[S]

case class Move[S](velocity: V2d) extends Action[S]
case class Nudge[S](targetId: AgentId, transform: S => S) extends Action[S]
case class ShareMemory[S](targetId: AgentId, event: S) extends Action[S]
case class MultiAction[S](actions: List[Action[S]]) extends Action[S]

object Action:
  def flatten[S](actions: List[Action[S]]): List[Action[S]] =
    @tailrec
    def _flatten(acc: List[Action[S]], remaining: List[Action[S]]): List[Action[S]] = remaining match
      case Nil                     => acc.reverse
      case MultiAction(inner) :: t => _flatten(acc, inner ++ t)
      case other :: t              => _flatten(other :: acc, t)
    _flatten(Nil, actions)
