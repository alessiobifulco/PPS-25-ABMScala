package gui

/** Type class defining monadic operations for a type constructor.
  *
  * @tparam M
  *   type constructor on which the monadic operations are defined.
  */
trait Monad[M[_]]:

  /** Wraps a value inside the monadic context.
    *
    * @param a
    *   value to wrap.
    * @tparam A
    *   type of the value.
    * @return
    *   the value wrapped in the monadic context.
    */
  def unit[A](a: A): M[A]

  /** Extension methods for values contained in the monadic context.
    */
  extension [A](m: M[A])

    /** Applies a function returning a value in the same monadic context.
      *
      * @param f
      *   function applied to the contained value.
      * @tparam B
      *   type of the value produced by the function.
      * @return
      *   the result of applying the function inside the monadic context.
      */
    def flatMap[B](f: A => M[B]): M[B]

    /** Transforms the value contained in the monadic context.
      *
      * @param f
      *   function used to transform the contained value.
      * @tparam B
      *   type of the transformed value.
      * @return
      *   the transformed value inside the monadic context.
      */
    def map[B](f: A => B): M[B] = m.flatMap(a => unit(f(a)))
