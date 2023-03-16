package jth.camera2.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jth.camera2.data.datasource.CameraRemoteSource
import jth.camera2.data.datasource.CameraRemoteSourceSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Singleton
    @Binds
    abstract fun bindsCameraRemoteSource(source: CameraRemoteSourceSourceImpl): CameraRemoteSource
}