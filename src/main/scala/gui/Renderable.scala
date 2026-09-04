package gui

import java.awt.Color

/** Type class used to define how a state is rendered.
  *
  * @tparam S
  *   type of the state to render.
  */
trait Renderable[S]:

  /** Returns the color associated with a state.
    *
    * @param state
    *   state to render.
    * @return
    *   the rendering color associated with the state.
    */
  def colorOf(state: S): Color

  /** Returns the textual label associated with a state.
    *
    * @param state
    *   state to render.
    * @return
    *   the textual representation of the state.
    */
  def labelOf(state: S): String = state.toString
