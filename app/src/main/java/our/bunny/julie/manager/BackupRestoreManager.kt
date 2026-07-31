package our.bunny.julie.manager

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import our.bunny.julie.data.local.PetDatabase
import our.bunny.julie.domain.model.*
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PetDatabase
) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    suspend fun exportData(outputUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dao = database.backupRestoreDao

            val backupData = BackupData(
                schemaVersion = 1,
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                pets = dao.getAllPets().map { 
                    PetBackup(
                        id = it.id, name = it.name, species = it.species, breed = it.breed, 
                        sex = it.sex, birthday = it.birthday?.toString(), adoptionDate = it.adoptionDate?.toString(),
                        weightUnit = it.weightUnit, color = it.color, notes = it.notes, microchipId = it.microchipId,
                        photoUri = it.photoUri, favoriteVet = it.favoriteVet, favoriteFood = it.favoriteFood,
                        isArchived = it.isArchived
                    )
                },
                weightLogs = dao.getAllWeightEntries().map {
                    WeightBackup(id = it.id, petId = it.petId, date = it.date.toString(), weight = it.weight, notes = it.notes)
                },
                waterLogs = dao.getAllWaterLogs().map {
                    WaterBackup(id = it.id, petId = it.petId, amount = it.amount, time = it.time.toString())
                },
                feedingLogs = dao.getAllFeedingLogs().map {
                    FeedingBackup(id = it.id, petId = it.petId, food = it.food, quantity = it.quantity, unit = it.unit, time = it.time.toString(), calories = it.calories, notes = it.notes, type = it.type)
                },
                medications = dao.getAllMedications().map {
                    MedicationBackup(id = it.id, petId = it.petId, name = it.name, dosage = it.dosage, medicationType = it.medicationType, isActive = it.isActive, notes = it.notes)
                },
                medicationSchedules = dao.getAllMedicationSchedules().map {
                    MedicationScheduleBackup(id = it.id, medicationId = it.medicationId, timeOfDay = it.timeOfDay, daysOfWeek = it.daysOfWeek)
                }
            )

            val jsonString = json.encodeToString(backupData)

            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
                outputStream.flush()
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun importData(inputUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: return@withContext Result.failure(Exception("Could not open input stream"))

            val backupData = json.decodeFromString<BackupData>(jsonString)

            if (backupData.schemaVersion > 1) {
                return@withContext Result.failure(Exception("Unsupported backup version: ${backupData.schemaVersion}"))
            }

            val dao = database.backupRestoreDao

            dao.restoreData(
                pets = backupData.pets.map { 
                    our.bunny.julie.data.local.entity.PetEntity(
                        id = it.id, name = it.name, species = it.species, breed = it.breed, 
                        sex = it.sex, birthday = it.birthday?.let { b -> java.time.LocalDate.parse(b) }, 
                        adoptionDate = it.adoptionDate?.let { a -> java.time.LocalDate.parse(a) },
                        weightUnit = it.weightUnit, color = it.color, notes = it.notes, 
                        microchipId = it.microchipId, photoUri = it.photoUri, 
                        favoriteVet = it.favoriteVet, favoriteFood = it.favoriteFood,
                        isArchived = it.isArchived
                    )
                },
                weightEntries = backupData.weightLogs.map {
                    our.bunny.julie.data.local.entity.WeightEntryEntity(
                        id = it.id, petId = it.petId, date = LocalDateTime.parse(it.date), weight = it.weight, notes = it.notes
                    )
                },
                waterLogs = backupData.waterLogs.map {
                    our.bunny.julie.data.local.entity.WaterLogEntity(
                        id = it.id, petId = it.petId, amount = it.amount, time = LocalDateTime.parse(it.time)
                    )
                },
                feedingLogs = backupData.feedingLogs.map {
                    our.bunny.julie.data.local.entity.FeedingLogEntity(
                        id = it.id, petId = it.petId, food = it.food, quantity = it.quantity, unit = it.unit, time = LocalDateTime.parse(it.time), calories = it.calories, notes = it.notes, type = it.type
                    )
                },
                medications = backupData.medications.map {
                    our.bunny.julie.data.local.entity.MedicationEntity(
                        id = it.id, petId = it.petId, name = it.name, dosage = it.dosage, medicationType = it.medicationType, isActive = it.isActive, notes = it.notes
                    )
                },
                medicationSchedules = backupData.medicationSchedules.map {
                    our.bunny.julie.data.local.entity.MedicationScheduleEntity(
                        id = it.id, medicationId = it.medicationId, timeOfDay = it.timeOfDay, daysOfWeek = it.daysOfWeek
                    )
                }
            )

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
