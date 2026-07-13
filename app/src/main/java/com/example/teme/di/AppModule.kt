package com.example.teme.di

import android.content.Context
import androidx.room.Room
import com.example.teme.data.local.AppDatabase
import com.example.teme.data.local.dao.PetDao
import com.example.teme.data.local.dao.RoomItemDao
import com.example.teme.data.repository.FocusRepositoryImpl
import com.example.teme.domain.repository.FocusRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "teme_database"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .build()
    }

    @Provides
    @Singleton
    fun providePetDao(database: AppDatabase): PetDao {
        return database.petDao()
    }

    @Provides
    @Singleton
    fun provideRoomItemDao(database: AppDatabase): RoomItemDao {
        return database.roomItemDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFocusRepository(
        focusRepositoryImpl: FocusRepositoryImpl
    ): FocusRepository
}
