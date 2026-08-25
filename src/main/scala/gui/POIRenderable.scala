package gui

import domain.POI
import java.awt.Color

/** Type class used to determine the color associated with a point of interest.
  */
trait POIRenderable:

  /** Returns the color used to render a point of interest.
    *
    * @param poi
    *   point of interest to render.
    * @return
    *   the color associated with the point of interest.
    */
  def colorOf(poi: POI): Color

/** Default instances for [[POIRenderable]].
  */
object POIRenderable:

  /** Default renderer that displays every point of interest in gray.
    */
  given default: POIRenderable with

    def colorOf(poi: POI): Color = Color.GRAY
