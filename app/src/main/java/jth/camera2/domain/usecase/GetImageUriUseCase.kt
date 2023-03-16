package jth.camera2.domain.usecase

import android.net.Uri
import jth.camera2.data.repository.CameraRepository
import java.io.File


class GetImageUriUseCase(private val repository: CameraRepository) {
    operator fun invoke(dir : File) : Uri = repository.getImageUri(dir)
}