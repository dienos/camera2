package jth.camera2.data.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RetryInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        var response: Response = chain.proceed(request)
        var tryCount = 0
        while (!response.isSuccessful && tryCount < 3) {
            tryCount++
            response = chain.proceed(request)
        }
        return response
    }
}