package domain

/** Represents an individual, autonomous entity operating within the simulation environment. An agent encapsulates both
  * physical properties (like position and velocity) and cognitive or domain-specific properties (like its internal
  * state and memory).
  *
  * @tparam S
  *   The generic type representing the domain-specific internal state of the Agent.
  */
trait Agent[S]:
  /** The unique identifier of the agent.
    */
  def id: AgentId

  /** The current spatial location of the agent in the environment.
    */
  def position: P2d

  /** The current movement vector, defining the agent's direction and speed.
    */
  def velocity: V2d

  /** The internal, domain-specific state of the agent.
    */
  def state: S

  /** The optional cognitive capacity of the agent, used to store past events or beliefs.
    */
  def memory: Option[Memory]

object Agent:

  /** Creates a new instance of an [[Agent]].
    *
    * @param id
    *   The unique [[AgentId]].
    * @param position
    *   The initial starting point ([[P2d]]).
    * @param velocity
    *   The initial movement vector ([[V2d]]).
    * @param state
    *   The initial internal state of type [[S]].
    * @param memory
    *   An optional [[Memory]] component, empty by default.
    * @return
    *   A fully instantiated [[Agent]].
    */
  def apply[S](
      id: AgentId,
      position: P2d,
      velocity: V2d,
      state: S,
      memory: Option[Memory] = Option.empty[Memory]
  ): Agent[S] = AgentImpl(id, position, velocity, state, memory)

  private case class AgentImpl[S](id: AgentId, position: P2d, velocity: V2d, state: S, memory: Option[Memory])
      extends Agent[S]

  /** Provides updated copies of the [[Agent]] to change its properties while preserving immutability.
    */
  extension [S](agent: Agent[S])

    /** Creates a new agent instance reflecting a change in its spatial properties.
      *
      * @param position
      *   The updated [[P2d]] location.
      * @param velocity
      *   The updated [[V2d]] movement vector.
      * @return
      *   A new [[Agent]] holding the updated position and velocity.
      */
    def withMotion(position: P2d, velocity: V2d): Agent[S] =
      Agent(agent.id, position, velocity, agent.state, agent.memory)

    /** Creates a new agent instance reflecting a change or transition in its internal state.
      *
      * @param state
      *   The newly evaluated state of type [[S]].
      * @return
      *   A new [[Agent]] holding the updated state.
      */
    def withState(state: S): Agent[S] = Agent(agent.id, agent.position, agent.velocity, state, agent.memory)

    /** Creates a new agent instance reflecting an update in its cognitive memory.
      *
      * @param memory
      *   The updated optional [[Memory]].
      * @return
      *   A new [[Agent]] holding the updated memory.
      */
    def withMemory(memory: Option[Memory]): Agent[S] =
      Agent(agent.id, agent.position, agent.velocity, agent.state, memory)

    /** Extracts all the beliefs currently held by the agent.
      *
      * @return
      *   A list of [[Belief]]s if the agent has a memory, otherwise an empty list.
      */
    def remembers: List[Belief] = agent.memory match
      case Some(m) => m.beliefs
      case _       => List.empty
