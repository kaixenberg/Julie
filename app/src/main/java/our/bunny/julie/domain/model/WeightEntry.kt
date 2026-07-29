package our.bunny.julie.domain.model

import java.time.LocalDateTime

data class WeightEntry(
    val id: Long = 0,
    val petId: Long,
    val date: LocalDateTime,
    val weight: Float,
    val unit: String,
    val notes: String
)
