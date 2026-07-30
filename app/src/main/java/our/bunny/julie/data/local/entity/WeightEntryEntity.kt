package our.bunny.julie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import our.bunny.julie.domain.model.WeightEntry
import java.time.LocalDateTime

@Entity(
    tableName = "weight_entries",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("petId")]
)
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val date: LocalDateTime,
    val weight: Float,
    val notes: String
) {
    fun toDomainModel(): WeightEntry {
        return WeightEntry(
            id = id,
            petId = petId,
            date = date,
            weight = weight,
            notes = notes
        )
    }

    companion object {
        fun fromDomainModel(model: WeightEntry): WeightEntryEntity {
            return WeightEntryEntity(
                id = model.id,
                petId = model.petId,
                date = model.date,
                weight = model.weight,
                notes = model.notes
            )
        }
    }
}
