package domain

/** The decision logic of an agent: given a context, produces the list of [[Action]]s the agent intends to perform in
  * the current tick.
  *
  * @tparam S
  *   the type of state used by agents in the simulation.
  * @tparam Ctx
  *   the type of context this behavior is evaluated against.
  */
trait Behavior[S, Ctx]:
  /** Evaluates this behavior against the given context.
    *
    * @param ctx
    *   the context to evaluate this behavior against.
    * @return
    *   the list of actions produced.
    */
  def apply(ctx: Ctx): List[Action[S]]

/** Factory and combinators for [[Behavior]].
  */
object Behavior:

  /** Creates a behavior from a plain function.
    *
    * @param f
    *   the function computing the actions from the context.
    * @tparam S
    *   the type of state used by agents in the simulation.
    * @tparam Ctx
    *   the type of context this behavior is evaluated against.
    * @return
    *   a new [[Behavior]] wrapping `f`.
    */
  def apply[S, Ctx](f: Ctx => List[Action[S]]): Behavior[S, Ctx] = f(_)

  /** Creates a behavior from an [[ActionGraph]], delegating the decision logic to it.
    *
    * @param graph
    *   the decision graph to resolve on each evaluation.
    * @tparam S
    *   the type of state used by agents in the simulation.
    * @tparam Ctx
    *   the type of context this behavior is evaluated against.
    * @return
    *   a new [[Behavior]] resolving `graph`.
    */
  def fromGraph[S, Ctx](graph: ActionGraph[S, Ctx]): Behavior[S, Ctx] = ctx => graph.resolve(ctx)

  extension [S, Ctx](b: Behavior[S, Ctx])
    /** Sequentially combines this behavior with `other`, producing the concatenation of the actions both would produce
      * on the same context.
      *
      * @param other
      *   the behavior to combine with this one.
      * @return
      *   a new [[Behavior]] producing the actions of both.
      */
    def andThen(other: Behavior[S, Ctx]): Behavior[S, Ctx] = ctx => b(ctx) ++ other(ctx)
