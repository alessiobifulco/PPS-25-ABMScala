import domain.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MemoryTest extends AnyFlatSpec with Matchers:

  private val memory = Memory(3)
  private val sighting = MemoryEvent.Sighting(PoiId(0), P2d(1.0, 1.0))
  private val encounter = MemoryEvent.Encounter(AgentId(2), true)

  "A memory" should "start with no beliefs" in:
    memory.beliefs shouldBe List.empty

  it should "reject a non positive capacity" in:
    an[IllegalArgumentException] should be thrownBy:
      Memory(0)

  it should "store a remembered event with its tick" in:
    memory.remember(1, sighting).beliefs shouldBe List(Belief(sighting, 1))

  it should "not modify the original memory" in:
    memory.remember(1, sighting)
    memory.beliefs shouldBe List.empty

  it should "keep only the most recent beliefs when it is full" in:
    val full = memory.remember(1, sighting).remember(2, encounter).remember(3, sighting).remember(4, encounter)
    full.beliefs should have size 3
    full.beliefs.head shouldBe Belief(encounter, 2)

  it should "have no latest belief when empty" in:
    memory.latest shouldBe None

  it should "expose the last remembered belief" in:
    memory.remember(1, sighting).remember(2, encounter).latest shouldBe Some(Belief(encounter, 2))

  it should "keep only the sightings among its beliefs" in:
    memory.remember(1, sighting).remember(2, encounter).sightings shouldBe List(Belief(sighting, 1))
