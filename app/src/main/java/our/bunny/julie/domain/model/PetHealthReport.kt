package our.bunny.julie.domain.model

data class PetHealthReport(
    val pet: Pet,
    val timelineEvents: List<TimelineEvent>,
    val medications: List<Medication>
)
