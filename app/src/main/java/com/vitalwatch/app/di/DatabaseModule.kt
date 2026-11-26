package com.vitalwatch.app.di

import android.content.Context
import androidx.room.Room
import com.vitalwatch.app.data.local.VitalDatabase
import com.vitalwatch.app.data.local.dao.ReadingDao
import com.vitalwatch.app.data.local.dao.SyncQueueDao
import com.vitalwatch.app.data.local.dao.UserSessionDao
import com.vitalwatch.app.data.repository.AuthRepositoryImpl
import com.vitalwatch.app.data.repository.ReadingRepositoryImpl
import com.vitalwatch.app.data.repository.SyncRepositoryImpl
import com.vitalwatch.app.domain.repository.AuthRepository
import com.vitalwatch.app.domain.repository.ReadingRepository
import com.vitalwatch.app.domain.repository.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindReadingRepository(impl: ReadingRepositoryImpl): ReadingRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    companion object {

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): VitalDatabase {
            return Room.databaseBuilder(
                context,
                VitalDatabase::class.java,
                "vitalwatch.db"
            ).fallbackToDestructiveMigration().build()
        }

        @Provides
        fun provideReadingDao(db: VitalDatabase): ReadingDao = db.readingDao()

        @Provides
        fun provideUserSessionDao(db: VitalDatabase): UserSessionDao = db.userSessionDao()

        @Provides
        fun provideSyncQueueDao(db: VitalDatabase): SyncQueueDao = db.syncQueueDao()
    }
}
