package jth.camera2.domain.usecase

import android.content.ContentResolver
import android.net.Uri
import jth.camera2.data.repository.CameraRepository

class GetRealPathFromUriUseCase(private val repository: CameraRepository) {
    operator fun invoke(resolver: ContentResolver, uri: Uri): String =
        repository.getRealPathFromUri(resolver, uri)
}