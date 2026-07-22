package domain

/** An agent participating in the simulation, parameterized over its own domain-specific state type `S`. Agents are
  * immutable: the public interface exposes no mutators, and implementations are expected to be backed by private,
  * value-based data (see [[Agent$.apply]]).
  *
  * @tparam S
  *   the type of state carried by this agent.
  */
trait Agent[S]:

  /** The unique identifier of this agent within the simulation.
    *
    * @return
    *   this agent's [[AgentId]].
    */
  def id: AgentId

  /** The current position of this agent.
    *
    * @return
    *   this agent's [[P2d]] position.
    */
  def position: P2d

  /** The current velocity of this agent.
    *
    * @return
    *   this agent's [[V2d]] velocity.
    */
  def velocity: V2d

  /** The domain-specific state of this agent (e.g. health, opinion, role).
    *
    * @return
    *   this agent's state.
    */
  def state: S

  /** This agent's memory of past events, if any.
    *
    * @return
    *   `Some` [[Memory]] if this agent has one, `None` otherwise.
    */
  def memory: Option[Memory[S]]

/** Factory for [[Agent]] instances.
  */
object Agent:

  /** Creates a new agent.
    *
    * @param id
    *   the unique identifier of the agent.
    * @param position
    *   the initial position.
    * @param velocity
    *   the initial velocity.
    * @param state
    *   the initial domain-specific state.
    * @param memory
    *   an optional memory, absent by default.
    * @tparam S
    *   the type of state carried by the agent.
    * @return
    *   a new immutable [[Agent]].
    */
  def apply[S](id: AgentId, position: P2d, velocity: V2d, state: S, memory: Option[Memory[S]] = None): Agent[S] =
    AgentImpl(id, position, velocity, state, memory)

  private case class AgentImpl[S](id: AgentId, position: P2d, velocity: V2d, state: S, memory: Option[Memory[S]])
      extends Agent[S]

/** The local perception of an agent during a behavior evaluation. Not a global view of the environment, but the set of
  * information an agent can use to decide its actions.
  *
  * @param focus
  *   the agent this context is evaluated for.
  * @param neighbors
  *   the agents visible to `focus` within the configured radius.
  * @param tick
  *   the current discrete tick of the simulation.
  * @tparam S
  *   the type of state used by agents in the simulation.
  */
case class AgentContext[S](focus: Agent[S], neighbors: List[Agent[S]], tick: Int)
