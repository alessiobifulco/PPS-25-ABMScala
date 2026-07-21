package domain

/** The memory of an agent, holding a history of past events. Currently, a minimal placeholder using the agent's own
  * state type `S` as a stand-in for recorded events; a dedicated event type and operations (capacity, recall, querying)
  * will be introduced once the memory model is designed.
  *
  * @tparam S
  *   the type of state used by agents in the simulation.
  */
trait Memory[S]

/** Factory for [[Memory]] instances.
  */
object Memory:

  /** Creates an empty memory with no recorded events.
    *
    * @tparam S
    *   the type of state used by agents in the simulation.
    * @return
    *   a new, empty [[Memory]].
    */
  def empty[S]: Memory[S] = MemoryImpl(Nil)

  private case class MemoryImpl[S](events: List[S]) extends Memory[S]
