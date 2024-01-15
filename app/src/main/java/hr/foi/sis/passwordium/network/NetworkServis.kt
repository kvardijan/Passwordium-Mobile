package hr.foi.sis.passwordium.network

import hr.foi.sis.passwordium.managers.JwtManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkServis {
    private const val URL = "https://passwordium-api.onrender.com/api/"

    private val instance: Retrofit by lazy {
        val trustManager: X509TrustManager = object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<out java.security.cert.X509Certificate>?,
                authType: String?,
            ) {
            }

            override fun checkServerTrusted(
                chain: Array<out java.security.cert.X509Certificate>?,
                authType: String?,
            ) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return emptyArray()
            }
        }

        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), java.security.SecureRandom())

        val client: OkHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

        Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val userServis: UserServis by lazy { instance.create(UserServis::class.java) }
    val keySerivs: KeyServis by lazy { instance.create(KeyServis::class.java)}
    val tokenSerivs: TokenServis by lazy { instance.create(TokenServis::class.java)}
    val accountServis: AccountServis by lazy { instance.create(AccountServis::class.java)}
}