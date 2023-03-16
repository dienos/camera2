package jth.camera2.domain.usecase

import android.content.ContentValues
import jth.camera2.data.repository.CameraRepository

class GetContentValuesUseCase(private val repository: CameraRepository) {
    operator fun invoke(isCroppedFile: Boolean, type: String, path: String): ContentValues =
        repository.getContentValues(isCroppedFile, type, path)
}