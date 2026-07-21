package domain

/** A declarative decision tree that, given a context, produces the list of [[Action]]s an agent intends to perform.
  * Used to writing a [[Behavior]] with nested pattern matching.
  *
  * @tparam S
  *   the type of state used by agents in the simulation.
  * @tparam Ctx
  *   the type of context used to resolve this graph.
  */
trait ActionGraph[S, Ctx]:
  /** Resolves this graph against the given context, producing the resulting actions.
    *
    * @param ctx
    *   the context to resolve this graph against.
    * @return
    *   the list of actions produced.
    */
  def resolve(ctx: Ctx): List[Action[S]]

/** Factory and building blocks for [[ActionGraph]].
  */
object ActionGraph:

  /** A leaf node, computing the actions to produce directly from the context. Carries a function rather than a fixed
    * [[Action]], so that a single action, multiple actions, or an amount of actions that depends on the context are all
    * expressible as a leaf.
    *
    * @param f
    *   the function computing the actions from the context.
    * @tparam S
    *   the type of state used by agents in the simulation.
    * @tparam Ctx
    *   the type of context used to resolve this graph.
    */
  case class Leaf[S, Ctx](f: Ctx => List[Action[S]]) extends ActionGraph[S, Ctx]:
    def resolve(ctx: Ctx): List[Action[S]] = f(ctx)

  /** A decision node, resolving to `ifTrue` when `condition` holds on the given context, to `ifFalse` otherwise.
    *
    * @param condition
    *   the predicate evaluated on the context.
    * @param ifTrue
    *   the sub-graph resolved when the condition holds.
    * @param ifFalse
    *   the sub-graph resolved when the condition does not hold.
    * @tparam S
    *   the type of state used by agents in the simulation.
    * @tparam Ctx
    *   the type of context used to resolve this graph.
    */
  case class Branch[S, Ctx](condition: Ctx => Boolean, ifTrue: ActionGraph[S, Ctx], ifFalse: ActionGraph[S, Ctx])
      extends ActionGraph[S, Ctx]:
    def resolve(ctx: Ctx): List[Action[S]] = if condition(ctx) then ifTrue.resolve(ctx) else ifFalse.resolve(ctx)
