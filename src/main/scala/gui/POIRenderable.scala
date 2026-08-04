package gui

import domain.POI
import java.awt.Color

trait POIRenderable[S]:
  def colorOf(poi: POI[S]): Color

object POIRenderable:
  given default[S]: POIRenderable[S] with
    def colorOf(poi: POI[S]): Color = Color.GRAY
