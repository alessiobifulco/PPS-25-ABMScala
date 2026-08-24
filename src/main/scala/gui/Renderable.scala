package gui

import java.awt.Color

trait Renderable[S]:
  def colorOf(state: S): Color
  def labelOf(state: S): String = state.toString
