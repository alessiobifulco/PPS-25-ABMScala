package domain

/** Represents the unique identifier of an [[Agent]] within the simulation. Defined as an opaque type over `Int` to
  * guarantee type safety, preventing it from being mixed up with plain integers while keeping the efficiency of a
  * primitive value at runtime.
  */
opaque type AgentId = Int

object AgentId:

  /** Wraps a raw integer into an [[AgentId]].
    *
    * @param value
    *   The primitive integer assigned to the agent.
    * @return
    *   The strongly-typed [[AgentId]].
    */
  def apply(value: Int): AgentId = value

  /** Provides access to the underlying primitive integer of the [[AgentId]].
    */
  extension (id: AgentId)

    /** @return
      *   The raw integer value of the identifier.
      */
    def value: Int = id
