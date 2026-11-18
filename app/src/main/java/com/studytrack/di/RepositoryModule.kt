package com.studytrack.di

import com.studytrack.data.repository.StudyTrackRepository
import com.studytrack.data.repository.StudyTrackRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStudyTrackRepository(
        impl: StudyTrackRepositoryImpl
    ): StudyTrackRepository
}
