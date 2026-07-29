package our.bunny.julie.domain.model

import java.time.LocalDateTime

sealed class TimelineEvent {
    abstract val timestamp: LocalDateTime

    data class WeightEvent(
        val entry: WeightEntry,
        override val timestamp: LocalDateTime = entry.date
    ) : TimelineEvent()

    data class FeedingEvent(
        val log: FeedingLog,
        override val timestamp: LocalDateTime = log.time
    ) : TimelineEvent()

    data class WaterEvent(
        val log: WaterLog,
        override val timestamp: LocalDateTime = log.time
    ) : TimelineEvent()

    // Medications typically have a "timeOfDay" String (e.g. "08:00") rather than a specific LocalDateTime.
    // For the timeline, we can either:
    // 1. Not show medications in the timeline (since they are recurring)
    // 2. Show when a medication was ADDED or UPDATED
    // Since we don't have a created_at for medication, we will exclude them from the timeline for now.
    // The timeline is better suited for discrete events (weight, feeding, water).
}
