package gui

import domain.POI
import java.awt.Color

trait POIRenderable:
  def colorOf(poi: POI): Color

object POIRenderable:
  given default: POIRenderable with
    def colorOf(poi: POI): Color = Color.GRAY
