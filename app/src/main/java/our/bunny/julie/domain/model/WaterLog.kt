package our.bunny.julie.domain.model

import java.time.LocalDateTime

data class WaterLog(
    val id: Long = 0,
    val petId: Long,
    val amount: Float,
    val time: LocalDateTime
)
