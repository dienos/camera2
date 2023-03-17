package jth.camera2.data.di

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jth.camera2.BuildConfig
import jth.camera2.data.api.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.*
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.*

@Module
@InstallIn(SingletonComponent::class)
internal object ApiModule {
    private const val TIMEOUT = 60L
    private const val BASE_URL_TEST = "https://blogif.aladin.co.kr/api/"
    private const val API_TYPE_BLOG = "B"
    private const val API_TYPE_WWW = "W"
    private const val API_TYPE_W_TEST = "if"
    private const val API_TYPE_TEST = "blogif"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {

            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {

            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        return OkHttpClient.Builder().apply {
            val logging = HttpLoggingInterceptor()

            if (BuildConfig.DEBUG) {
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            } else {
                logging.setLevel(HttpLoggingInterceptor.Level.NONE)
            }

            addInterceptor(logging)
            addInterceptor(ResponseInterceptor())
            addInterceptor(RetryInterceptor())
            sslSocketFactory(
                SSLSocketFactoryExtended(),
                trustAllCerts[0] as X509TrustManager
            )
            hostnameVerifier(HostnameVerifier { _: String?, _: SSLSession? -> true })
            followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .cache(null)
            connectTimeout(
                TIMEOUT,
                TimeUnit.SECONDS
            )
                .readTimeout(
                    TIMEOUT,
                    TimeUnit.SECONDS
                )
                .writeTimeout(
                    TIMEOUT,
                    TimeUnit.SECONDS
                )
        }.build()
    }

    @Provides
    @Singleton
    fun providesRetrofit(
        client: OkHttpClient,
        @ApplicationContext context: Context
    ): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(
                if (BuildConfig.DEBUG) {
                    BuildConfig.BASE_URL
                } else {
                    BPAccess.getUrl_B(context)
                }
            )
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun providesService(retrofit: Retrofit): SampleService {
        return retrofit.create(SampleService::class.java)
    }
}

