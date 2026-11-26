package com.vitalwatch.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vitalwatch.app.data.local.dao.ReadingDao
import com.vitalwatch.app.data.local.dao.SyncQueueDao
import com.vitalwatch.app.data.local.dao.UserSessionDao
import com.vitalwatch.app.data.local.entity.ReadingEntity
import com.vitalwatch.app.data.local.entity.SyncQueueEntity
import com.vitalwatch.app.data.local.entity.UserSessionEntity

@Database(
    entities = [
        ReadingEntity::class,
        UserSessionEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VitalDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao
    abstract fun userSessionDao(): UserSessionDao
    abstract fun syncQueueDao(): SyncQueueDao
}
