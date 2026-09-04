import domain.{P2d, POI, PoiId}
import gui.POIRenderable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.awt.Color

class POIRenderableTest extends AnyFlatSpec, Matchers:

  private val poi = POI(PoiId(1), "Test POI", P2d(10.0, 10.0), 5.0)

  "POIRenderable default instance" should "render POIs in gray" in:
    val renderable = POIRenderable.default

    renderable.colorOf(poi) shouldBe Color.GRAY
