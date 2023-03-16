package jth.camera2.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import jth.camera2.data.repository.CameraRepositoryImpl
import jth.camera2.domain.usecase.GetContentValuesUseCase
import jth.camera2.domain.usecase.GetFileNameUseCase
import jth.camera2.domain.usecase.GetImageUriUseCase
import jth.camera2.domain.usecase.GetRealPathFromUriUseCase

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    fun providesGetImageUriUseCase(repository: CameraRepositoryImpl): GetImageUriUseCase {
        return GetImageUriUseCase(repository)
    }

    @Provides
    fun providesGetFileNameUseCase(repository: CameraRepositoryImpl): GetFileNameUseCase {
        return GetFileNameUseCase(repository)
    }

    @Provides
    fun providesGetContentValuesUseCase(repository: CameraRepositoryImpl): GetContentValuesUseCase {
        return GetContentValuesUseCase(repository)
    }

    @Provides
    fun providesGetRealPathFromUriUseCase(repository: CameraRepositoryImpl): GetRealPathFromUriUseCase {
        return GetRealPathFromUriUseCase(repository)
    }
}