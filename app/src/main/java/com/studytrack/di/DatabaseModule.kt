package com.studytrack.di

import android.content.Context
import androidx.room.Room
import com.studytrack.data.dao.ProjectDao
import com.studytrack.data.dao.TimeEntryDao
import com.studytrack.data.db.StudyTrackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudyTrackDatabase =
        Room.databaseBuilder(
            context,
            StudyTrackDatabase::class.java,
            StudyTrackDatabase.DATABASE_NAME
        ).build()

    @Provides
    fun provideTimeEntryDao(db: StudyTrackDatabase): TimeEntryDao = db.timeEntryDao()

    @Provides
    fun provideProjectDao(db: StudyTrackDatabase): ProjectDao = db.projectDao()
}
