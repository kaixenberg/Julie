package our.bunny.julie.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import our.bunny.julie.data.local.PetDatabase
import our.bunny.julie.data.repository.PetRepositoryImpl
import our.bunny.julie.domain.repository.PetRepository
import our.bunny.julie.data.local.dao.TrackerDao
import our.bunny.julie.data.repository.TrackerRepositoryImpl
import our.bunny.julie.domain.repository.TrackerRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePetDatabase(app: Application): PetDatabase {
        return Room.databaseBuilder(
            app,
            PetDatabase::class.java,
            PetDatabase.DATABASE_NAME
        )
        .addMigrations(PetDatabase.MIGRATION_4_5, PetDatabase.MIGRATION_5_6, PetDatabase.MIGRATION_6_7)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun providePetRepository(db: PetDatabase): PetRepository {
        return PetRepositoryImpl(db.petDao)
    }

    @Provides
    @Singleton
    fun provideTrackerRepository(db: PetDatabase): TrackerRepository {
        return TrackerRepositoryImpl(db.trackerDao)
    }

    @Provides
    @Singleton
    fun provideExportRepository(
        petRepository: PetRepository,
        trackerRepository: TrackerRepository
    ): our.bunny.julie.domain.repository.ExportRepository {
        return our.bunny.julie.data.repository.ExportRepositoryImpl(petRepository, trackerRepository)
    }
}
