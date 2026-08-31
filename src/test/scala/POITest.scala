import domain.{P2d, POI, PoiId, Residency}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class POITest extends AnyFlatSpec, Matchers:

  private val poiId = PoiId(1)
  private val position = P2d(10.0, 10.0)
  private val radius = 5.0

  "PoiId" should "preserve its integer value" in:
    poiId.value shouldBe 1

  "POI" should "accept valid parameters" in:
    val poi = POI(poiId, "Test POI", position, radius)

    poi.id shouldBe poiId
    poi.name shouldBe "Test POI"
    poi.position shouldBe position
    poi.radius shouldBe radius
    poi.activationDelay shouldBe 0

  it should "accept a non-negative activation delay" in:
    POI(poiId, "Test POI", position, radius, activationDelay = 3).activationDelay shouldBe 3

  it should "reject a non-positive radius" in:
    an[IllegalArgumentException] should be thrownBy:
      POI(poiId, "Test POI", position, 0.0)

    an[IllegalArgumentException] should be thrownBy:
      POI(poiId, "Test POI", position, -1.0)

  it should "reject a negative activation delay" in:
    an[IllegalArgumentException] should be thrownBy:
      POI(poiId, "Test POI", position, radius, activationDelay = -1)

  it should "contain its center" in:
    val poi = POI(poiId, "Test POI", position, radius)

    poi.contains(position) shouldBe true

  it should "contain a position inside its radius" in:
    val poi = POI(poiId, "Test POI", position, radius)

    poi.contains(P2d(12.0, 10.0)) shouldBe true

  it should "contain a position on its boundary" in:
    val poi = POI(poiId, "Test POI", position, radius)

    poi.contains(P2d(15.0, 10.0)) shouldBe true

  it should "reject a position outside its radius" in:
    val poi = POI(poiId, "Test POI", position, radius)

    poi.contains(P2d(16.0, 10.0)) shouldBe false

  "Residency" should "start empty" in:
    Residency.empty.perPoi shouldBe Map.empty

  it should "return zero ticks for an unknown POI" in:
    Residency.empty.ticksIn(poiId) shouldBe 0

  it should "increase the ticks for an existing POI" in:
    val residency = Residency(Map(poiId -> 2))

    residency.tickFor(poiId).ticksIn(poiId) shouldBe 3

  it should "start counting from one for an unknown POI" in:
    Residency.empty.tickFor(poiId).ticksIn(poiId) shouldBe 1

  it should "preserve the original residency when ticking" in:
    val residency = Residency(Map(poiId -> 2))

    residency.tickFor(poiId)
    residency.ticksIn(poiId) shouldBe 2

  it should "reset a POI residency" in:
    val residency = Residency(Map(poiId -> 3))

    residency.reset(poiId).ticksIn(poiId) shouldBe 0

  it should "preserve other POI residencies when resetting one" in:
    val otherPoiId = PoiId(2)
    val residency = Residency(Map(poiId -> 3, otherPoiId -> 5))

    residency.reset(poiId).perPoi shouldBe Map(otherPoiId -> 5)

  it should "leave the original residency unchanged when resetting" in:
    val residency = Residency(Map(poiId -> 3))

    residency.reset(poiId)
    residency.ticksIn(poiId) shouldBe 3
