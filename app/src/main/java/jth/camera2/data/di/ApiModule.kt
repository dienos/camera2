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
import jth.camera2.data.api.SampleService
import jth.camera2.data.di.ApiModule.API_TYPE_TEST
import jth.camera2.data.di.ApiModule.API_TYPE_WWW
import jth.camera2.data.di.ApiModule.API_TYPE_W_TEST
import jth.camera2.data.di.ApiModule.BASE_URL_TEST
import jth.camera2.data.di.ApiModule.getUrlB
import jth.camera2.data.di.ApiModule.getUrlW
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.*

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    private val debug = false

    private const val TIMEOUT = 60L
    const val BASE_URL_TEST = "https://blogif.aladin.co.kr/api/"
    const val API_TYPE_BLOG = "B"
    const val API_TYPE_WWW = "W"
    const val API_TYPE_W_TEST = "if"
    const val API_TYPE_TEST = "blogif"

    fun getUrlB(context: Context): String {
//		return "https://blog.aladin.co.kr/api/";
        return "https://blogif.aladin.co.kr/api/"
//		return "http://blogdev.aladin.co.kr/api/";
//		return "http://blogif.aladin.co.kr/api/";
        /*if (Debug) {
			return getAzureAccessKey(context, "B","FrLG5c1ocnQ8DQXIOMh3PMwR6pvD+Eo9090xsoRMMOw=");
		} else {
			return getAzureAccessKey(context,"B","AUiyZuFgri7hQLVY5PGFrb9rOMXsKGawVUloOv8eBiA=");
		}*/
    }

    fun getUrlW(context: Context): String {
        return "https://www.aladin.co.kr/"
//		return "http://aa.aladin.co.kr/";
//		 return "https://juliet.aladin.co.kr/";
//		return "https://stage.aladin.co.kr/";
        /*if (Debug) {
			return getAzureAccessKey(context, "W","eKk1RY+ov07DJ1bkgI8bbxjt6sXaFQj+XIF3raOtObo=");
		} else {
			return getAzureAccessKey(context,"W","wLF/4xtjZGfavZSs371vpuJyqSsNl9b8nbdmiIxYoVU=");
		}*/
    }

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
            if (debug) {
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
            hostnameVerifier(HostnameVerifier { hostname: String?, session: SSLSession? -> true })
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
}

private class ResponseInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
        chain.request().newBuilder()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .addHeader("ENCTYPE", "multipart/form-data").build()
    )
}

private class RetryInterceptor : Interceptor {
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

internal class SSLSocketFactoryExtended : SSLSocketFactory() {
    private lateinit var mSSLContext: SSLContext
    private lateinit var mCiphers: Array<String>
    private lateinit var mProtocols: Array<String>

    override fun getDefaultCipherSuites(): Array<String> {
        return mCiphers
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return mCiphers
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        val factory = mSSLContext!!.socketFactory
        val ss = factory.createSocket(s, host, port, autoClose) as SSLSocket
        ss.enabledProtocols = mProtocols
        ss.enabledCipherSuites = mCiphers
        return ss
    }

    @Throws(IOException::class)
    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int
    ): Socket {
        val factory = mSSLContext!!.socketFactory
        val ss = factory.createSocket(address, port, localAddress, localPort) as SSLSocket
        ss.enabledProtocols = mProtocols
        ss.enabledCipherSuites = mCiphers
        return ss
    }

    @Throws(IOException::class)
    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int
    ): Socket {
        val factory = mSSLContext!!.socketFactory
        val ss = factory.createSocket(host, port, localHost, localPort) as SSLSocket
        ss.enabledProtocols = mProtocols
        ss.enabledCipherSuites = mCiphers
        return ss
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        val factory = mSSLContext!!.socketFactory
        val ss = factory.createSocket(host, port) as SSLSocket
        ss.enabledProtocols = mProtocols
        ss.enabledCipherSuites = mCiphers
        return ss
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int): Socket {
        val factory = mSSLContext!!.socketFactory
        val ss = factory.createSocket(host, port) as SSLSocket
        ss.enabledProtocols = mProtocols
        ss.enabledCipherSuites = mCiphers
        return ss
    }

    @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    private fun initSSLSocketFactoryEx(
        km: Array<KeyManager>?,
        tm: Array<TrustManager>?,
        random: SecureRandom?
    ) {
        mSSLContext = SSLContext.getInstance("TLS")
        mSSLContext.init(km, tm, random)
        mProtocols = GetProtocolList()
        mCiphers = GetCipherList()
    }

    protected fun GetProtocolList(): Array<String> {
        val protocols = arrayOf("TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3")
        var availableProtocols: Array<String?>? = null
        var socket: SSLSocket? = null
        try {
            val factory = mSSLContext.socketFactory
            socket = factory.createSocket() as SSLSocket
            availableProtocols = socket.supportedProtocols
        } catch (e: java.lang.Exception) {
            return arrayOf("TLSv1")
        } finally {
            if (socket != null) try {
                socket.close()
            } catch (e: IOException) {
            }
        }
        val resultList: MutableList<String> = ArrayList()
        for (i in protocols.indices) {
            val idx = Arrays.binarySearch(availableProtocols, protocols[i])
            if (idx >= 0) resultList.add(protocols[i])
        }
        return resultList.toTypedArray()
    }

    protected fun GetCipherList(): Array<String> {
        val resultList: MutableList<String> = ArrayList()
        val factory = mSSLContext.socketFactory
        for (s in factory.supportedCipherSuites) {
            resultList.add(s)
        }
        return resultList.toTypedArray()
    }

    init {
        initSSLSocketFactoryEx(null, null, null)
    }
}

@Provides
@Singleton
fun providesRetrofit(
    client: OkHttpClient,
    @ApplicationContext context: Context,
): Retrofit? {
    val type = API_TYPE_WWW
    var url: String = getUrlB(context)

    if (!TextUtils.isEmpty(type)) {
        when (type) {
            API_TYPE_WWW -> url = getUrlW(context)
            API_TYPE_W_TEST -> url = "https://if.aladin.co.kr/"
            API_TYPE_TEST -> url = BASE_URL_TEST
        }
    }
    val gson = GsonBuilder()
        .setLenient()
        .create()

    return Retrofit.Builder()
        .baseUrl(url) //                    .addConverterFactory(new NullOnEmptyConverterFactory())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()
}

@Provides
@Singleton
fun providesService(retrofit: Retrofit): SampleService {
    return retrofit.create(SampleService::class.java)
}