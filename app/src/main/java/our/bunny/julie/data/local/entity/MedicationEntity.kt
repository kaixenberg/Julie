package our.bunny.julie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import our.bunny.julie.domain.model.Medication

@Entity(
    tableName = "medications",
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
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val name: String,
    val dosage: String,
    val frequency: String,
    val timeOfDay: String,
    val isActive: Boolean,
    val notes: String
) {
    fun toDomainModel(): Medication {
        return Medication(
            id = id,
            petId = petId,
            name = name,
            dosage = dosage,
            frequency = frequency,
            timeOfDay = timeOfDay,
            isActive = isActive,
            notes = notes
        )
    }

    companion object {
        fun fromDomainModel(model: Medication): MedicationEntity {
            return MedicationEntity(
                id = model.id,
                petId = model.petId,
                name = model.name,
                dosage = model.dosage,
                frequency = model.frequency,
                timeOfDay = model.timeOfDay,
                isActive = model.isActive,
                notes = model.notes
            )
        }
    }
}
