package jth.camera2.data.datasource

interface CameraRemoteSource {
    suspend fun getSimple()
}