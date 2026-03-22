package dev.parcelview.backend.service

import com.google.common.truth.Truth.assertThat
import dev.parcelview.backend.courier.Courier
import dev.parcelview.backend.courier.CourierClient
import dev.parcelview.backend.courier.CourierClientRegistry
import dev.parcelview.backend.courier.CourierStatus
import dev.parcelview.backend.entity.TrackingEvent
import dev.parcelview.backend.entity.TrackingInfo
import dev.parcelview.backend.repository.TrackingInfoRepository
import dev.parcelview.backend.service.exceptions.TrackingException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TrackingServiceTest {

    private val trackingInfoRepository = mockk<TrackingInfoRepository>()
    private val courierClientRegistry = mockk<CourierClientRegistry>()
    private val courierClient = mockk<CourierClient>()

    private val service = TrackingService(trackingInfoRepository, courierClientRegistry)

    private val now = Clock.System.now()
    private val fakeTrackingNumber = "fake-tracking-number"
    private val fakeCourierName = "usps"

    private val supportedCarriers = setOf("usps", "fedex", "dhl")

    private fun makeEvent(
        id: UUID? = UUID.randomUUID(),
        timestamp: kotlin.time.Instant = now,
        status: CourierStatus = CourierStatus.IN_TRANSIT,
        eventCode: String = "CODE1",
    ) = TrackingEvent(
        id = id,
        timestamp = timestamp,
        courier = Courier.USPS,
        status = status,
        eventCode = eventCode,
    )

    private fun makeTrackingInfo(
        id: UUID? = UUID.randomUUID(),
        status: CourierStatus = CourierStatus.IN_TRANSIT,
        lastUpdated: kotlin.time.Instant = now,
        events: MutableSet<TrackingEvent> = mutableSetOf(),
    ) = TrackingInfo(
        id = id,
        trackingNumber = fakeTrackingNumber,
        courier = fakeCourierName,
        status = status,
        lastUpdated = lastUpdated,
        events = events,
    )

    @BeforeEach
    fun setUp() {
        every { courierClientRegistry.getCourier(fakeCourierName) } returns courierClient
        every { courierClientRegistry.supportedCouriers() } returns supportedCarriers
    }

    @Nested
    inner class CourierNotFound {

        @Test
        fun `throws CourierNotFoundException when courier is not registered`() = runTest {
            every { courierClientRegistry.getCourier("unknown") } returns null
            every { trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(any(), any()) } returns null

            val ex = assertThrows<TrackingException.CourierNotFoundException> {
                service.getTracking(fakeTrackingNumber, "unknown")
            }

            assertThat(ex.courier).isEqualTo("unknown")
            assertThat(ex.supportedCouriers).containsExactlyElementsIn(supportedCarriers)
        }

        @Test
        fun `error message contains the unsupported courier name`() = runTest {
            every { courierClientRegistry.getCourier("unknown") } returns null
            every { trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(any(), any()) } returns null

            val ex = assertThrows<TrackingException.CourierNotFoundException> {
                service.getTracking(fakeTrackingNumber, "unknown")
            }

            assertThat(ex.message).contains("unknown")
        }
    }

    @Nested
    inner class CachingLogic {

        @Test
        fun `returns cached result without fetching when status is DELIVERED`() = runTest {
            val cached = makeTrackingInfo(status = CourierStatus.DELIVERED)
            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns cached

            val result = service.getTracking(fakeTrackingNumber, fakeCourierName)

            assertThat(result.id).isEqualTo(cached.id)
            coVerify(exactly = 0) { courierClient.fetchTracking(any()) }
        }

        @Test
        fun `returns cached result without fetching when lastUpdated is within 1 hour`() = runTest {
            val cached = makeTrackingInfo(lastUpdated = now.minus(30.minutes))
            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns cached

            val result = service.getTracking(fakeTrackingNumber, fakeCourierName)

            assertThat(result.id).isEqualTo(cached.id)
            coVerify(exactly = 0) { courierClient.fetchTracking(any()) }
        }

        @Test
        fun `fetches fresh data when cache is stale (older than 1 hour)`() = runTest {
            val staleTime = now.minus(2.hours)
            val cached = makeTrackingInfo(lastUpdated = staleTime)
            val fresh = makeTrackingInfo(id = null)

            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns cached
            coEvery { courierClient.fetchTracking(fakeTrackingNumber) } returns fresh
            every { trackingInfoRepository.save(any()) } answers { firstArg() }

            service.getTracking(fakeTrackingNumber, fakeCourierName)

            coVerify(exactly = 1) { courierClient.fetchTracking(fakeTrackingNumber) }
        }

        @Test
        fun `fetches fresh data when no cache entry exists`() = runTest {
            val fresh = makeTrackingInfo(id = null)

            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns null
            coEvery { courierClient.fetchTracking(fakeTrackingNumber) } returns fresh
            every { trackingInfoRepository.save(any()) } answers { firstArg() }

            service.getTracking(fakeTrackingNumber, fakeCourierName)

            coVerify(exactly = 1) { courierClient.fetchTracking(fakeTrackingNumber) }
        }

        @Test
        fun `preserves cached id when saving fresh data`() = runTest {
            val cachedId = UUID.randomUUID()
            val cached = makeTrackingInfo(id = cachedId, lastUpdated = now.minus(2.hours))
            val fresh = makeTrackingInfo(id = null)
            val savedSlot = slot<TrackingInfo>()

            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns cached
            coEvery { courierClient.fetchTracking(fakeTrackingNumber) } returns fresh
            every { trackingInfoRepository.save(capture(savedSlot)) } answers { firstArg() }

            service.getTracking(fakeTrackingNumber, fakeCourierName)

            assertThat(savedSlot.captured.id).isEqualTo(cachedId)
        }

        @Test
        fun `returned cached events are sorted descending by timestamp`() = runTest {
            val earlier = makeEvent(timestamp = now.minus(2.hours))
            val later = makeEvent(timestamp = now.minus(1.hours))
            val cached = makeTrackingInfo(
                status = CourierStatus.DELIVERED,
                events = mutableSetOf(earlier, later),
            )

            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns cached

            val result = service.getTracking(fakeTrackingNumber, fakeCourierName)

            assertThat(result.events.first().timestamp).isEqualTo(later.timestamp)
            assertThat(result.events.last().timestamp).isEqualTo(earlier.timestamp)
        }
    }

    @Nested
    inner class MergeLogic {

        @Test
        fun `incoming events with no cache produces events with null ids`() = runTest {
            val incomingEvent = makeEvent(id = null, timestamp = now)
            val fresh = makeTrackingInfo(id = null, events = mutableSetOf(incomingEvent))

            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns null
            coEvery { courierClient.fetchTracking(fakeTrackingNumber) } returns fresh
            val savedSlot = slot<TrackingInfo>()
            every { trackingInfoRepository.save(capture(savedSlot)) } answers { firstArg() }

            service.getTracking(fakeTrackingNumber, fakeCourierName)

            val firstSave = savedSlot.captured
            assertThat(firstSave.events).hasSize(1)
            assertThat(firstSave.events.first().id).isNull()
        }

        @Test
        fun `matching cached event id is preserved for same timestamp`() = runTest {
            val sharedTimestamp = now.minus(1.hours)
            val existingId = UUID.randomUUID()

            val cachedEvent = makeEvent(id = existingId, timestamp = sharedTimestamp)
            val cached = makeTrackingInfo(lastUpdated = now.minus(2.hours), events = mutableSetOf(cachedEvent))

            val incomingEvent = makeEvent(id = null, timestamp = sharedTimestamp)
            val fresh = makeTrackingInfo(id = null, events = mutableSetOf(incomingEvent))

            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns cached
            coEvery { courierClient.fetchTracking(fakeTrackingNumber) } returns fresh
            val savedSlot = slot<TrackingInfo>()
            every { trackingInfoRepository.save(capture(savedSlot)) } answers { firstArg() }

            service.getTracking(fakeTrackingNumber, fakeCourierName)

            val mergedEvent = savedSlot.captured.events.first()
            assertThat(mergedEvent.id).isEqualTo(existingId)
        }

        @Test
        fun `new incoming event with no matching timestamp gets null id`() = runTest {
            val oldTimestamp = now.minus(3.hours)
            val newTimestamp = now.minus(1.hours)

            val cachedEvent = makeEvent(id = UUID.randomUUID(), timestamp = oldTimestamp)
            val cached = makeTrackingInfo(lastUpdated = now.minus(2.hours), events = mutableSetOf(cachedEvent))

            val incomingEvent = makeEvent(id = null, timestamp = newTimestamp)
            val fresh = makeTrackingInfo(id = null, events = mutableSetOf(incomingEvent))

            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns cached
            coEvery { courierClient.fetchTracking(fakeTrackingNumber) } returns fresh
            val savedSlot = slot<TrackingInfo>()
            every { trackingInfoRepository.save(capture(savedSlot)) } answers { firstArg() }

            service.getTracking(fakeTrackingNumber, fakeCourierName)

            assertThat(savedSlot.captured.events).hasSize(1)
            assertThat(savedSlot.captured.events.first().id).isNull()
        }

        @Test
        fun `merge produces correct count when incoming has more events than cache`() = runTest {
            val t1 = now.minus(3.hours)
            val t2 = now.minus(2.hours)
            val t3 = now.minus(1.hours)

            val cachedEvent = makeEvent(id = UUID.randomUUID(), timestamp = t1)
            val cached = makeTrackingInfo(lastUpdated = now.minus(2.hours), events = mutableSetOf(cachedEvent))

            val fresh = makeTrackingInfo(
                id = null,
                events = mutableSetOf(
                    makeEvent(id = null, timestamp = t1),
                    makeEvent(id = null, timestamp = t2),
                    makeEvent(id = null, timestamp = t3),
                ),
            )

            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns cached
            coEvery { courierClient.fetchTracking(fakeTrackingNumber) } returns fresh
            val savedSlot = slot<TrackingInfo>()
            every { trackingInfoRepository.save(capture(savedSlot)) } answers { firstArg() }

            service.getTracking(fakeTrackingNumber, fakeCourierName)

            assertThat(savedSlot.captured.events).hasSize(3)
        }

        // In theory this case should never happen, but it's worth testing anyway because of my logic
        @Test
        fun `merge does not produce duplicate events for same timestamp`() = runTest {
            val sharedTimestamp = now.minus(1.hours)

            val cachedEvent = makeEvent(id = UUID.randomUUID(), timestamp = sharedTimestamp)
            val cached = makeTrackingInfo(lastUpdated = now.minus(2.hours), events = mutableSetOf(cachedEvent))

            val fresh = makeTrackingInfo(
                id = UUID.randomUUID(),
                events = mutableSetOf(
                    makeEvent(id = UUID.randomUUID(), timestamp = sharedTimestamp, eventCode = "CODE1"),
                    makeEvent(id = UUID.randomUUID(), timestamp = sharedTimestamp, eventCode = "CODE2"),
                ),
            )

            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns cached
            coEvery { courierClient.fetchTracking(fakeTrackingNumber) } returns fresh
            val savedSlot = slot<TrackingInfo>()
            every { trackingInfoRepository.save(capture(savedSlot)) } answers { firstArg() }

            service.getTracking(fakeTrackingNumber, fakeCourierName)

            assertThat(savedSlot.captured.events).hasSize(1)
        }

        @Test
        fun `trackingInfoId is backfilled on events after second save`() = runTest {
            val savedId = UUID.randomUUID()
            val incomingEvent = makeEvent(id = null, timestamp = now)
            val fresh = makeTrackingInfo(id = null, events = mutableSetOf(incomingEvent))
            val afterFirstSave = fresh.copy(id = savedId)

            every {
                trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(fakeTrackingNumber, fakeCourierName)
            } returns null
            coEvery { courierClient.fetchTracking(fakeTrackingNumber) } returns fresh

            val saveArgs = mutableListOf<TrackingInfo>()
            every { trackingInfoRepository.save(capture(saveArgs)) } answers {
                if (saveArgs.size == 1) afterFirstSave else firstArg()
            }

            service.getTracking(fakeTrackingNumber, fakeCourierName)

            assertThat(saveArgs).hasSize(2)
            val secondSaveArg = saveArgs[1]
            assertThat(secondSaveArg.events).isNotEmpty()
            secondSaveArg.events.forEach { event ->
                assertThat(event.trackingInfoId).isEqualTo(savedId)
            }
        }
    }
}
