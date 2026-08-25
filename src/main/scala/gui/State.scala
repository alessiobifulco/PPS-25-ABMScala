package gui

/** Represents a computation that transforms an input state into a new state and produces a result value.
  *
  * @param run
  *   function used to execute the stateful computation.
  * @tparam S
  *   type of the state.
  * @tparam A
  *   type of the result value.
  */
case class State[S, A](run: S => (S, A))

object State:

  /** Executes the stateful computation using the provided state.
    */
  extension [S, A](stateMonadValue: State[S, A])

    /** Applies the stateful computation to the provided state.
      *
      * @param currentState
      *   initial state used for the computation.
      * @return
      *   a tuple containing the updated state and the result value.
      */
    def apply(currentState: S): (S, A) = stateMonadValue match
      case State(run) => run(currentState)

  /** Monad instance for stateful computations.
    *
    * @tparam S
    *   type of the state.
    */
  given stateMonad[S]: Monad[[A] =>> State[S, A]] with

    /** Creates a stateful computation that returns the provided value without modifying the state.
      *
      * @return
      *   a stateful computation containing the provided value.
      */
    def unit[A](value: A): State[S, A] = State(currentState => (currentState, value))

    extension [A](stateMonadValue: State[S, A])

      /** Combines the current stateful computation with a subsequent computation.
        *
        * @param transition
        *   function used to generate the subsequent computation.
        * @tparam B
        *   type of the result value produced by the subsequent computation.
        * @return
        *   a stateful computation that applies both computations in sequence.
        */
      override def flatMap[B](transition: A => State[S, B]): State[S, B] = State(currentState =>
        stateMonadValue.apply(currentState) match
          case (nextState, value) => transition(value).apply(nextState)
      )
