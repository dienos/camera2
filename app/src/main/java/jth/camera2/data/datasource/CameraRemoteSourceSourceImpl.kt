package jth.camera2.data.datasource

import jth.camera2.data.api.SampleService
import javax.inject.Inject


class CameraRemoteSourceSourceImpl @Inject constructor(
    private val sampleService: SampleService
) : CameraRemoteSource {
    override suspend fun getSimple() {
    }
}