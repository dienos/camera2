package jth.camera2.data.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ResponseInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
        chain.request().newBuilder()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .addHeader("ENCTYPE", "multipart/form-data").build()
    )
}
