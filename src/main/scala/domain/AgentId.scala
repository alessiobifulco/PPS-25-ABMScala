package domain

/** A type-safe identifier for an [[Agent]]. Defined as an opaque alias over `Int` rather than exposing `Int` directly,
  * to prevent accidental mixins with other integer-typed identifiers used in the simulation (e.g. a POI id) when
  * routing actions such as `Nudge` or `ShareMemory` towards a specific agent. Compiles down to a plain `Int` at
  * runtime, with no additional overhead.
  */
opaque type AgentId = Int

/** Factory and utilities for [[AgentId]].
  */
object AgentId:

  /** Wraps a raw `Int` as an [[AgentId]].
    *
    * @param value
    *   the underlying integer value.
    * @return
    *   a new [[AgentId]] wrapping `value`.
    */
  def apply(value: Int): AgentId = value

  extension (id: AgentId)

    /** The underlying `Int` value of this id.
      *
      * @return
      *   the raw integer value wrapped by this [[AgentId]].
      */
    def value: Int = id
