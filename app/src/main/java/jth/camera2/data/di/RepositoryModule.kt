package jth.camera2.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jth.camera2.data.repository.CameraRepository
import jth.camera2.data.repository.CameraRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindsCameraRepository(
        repository: CameraRepositoryImpl
    ): CameraRepository
}