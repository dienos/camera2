package jth.camera2.domain.usecase

import jth.camera2.data.repository.CameraRepository

class GetFileNameUseCase(private val repository: CameraRepository) {
    operator fun invoke() : String = repository.getFileName()
}