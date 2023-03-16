package jth.camera2.data.api

import retrofit2.http.GET

interface SampleService {
    @GET("sample")
    suspend fun getSample()
}