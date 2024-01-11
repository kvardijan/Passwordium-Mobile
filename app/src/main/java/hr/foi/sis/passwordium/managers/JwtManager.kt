package hr.foi.sis.passwordium.managers

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import hr.foi.sis.passwordium.models.UserResponse
import java.util.TimeZone
import hr.foi.sis.passwordium.BuildConfig
import hr.foi.sis.passwordium.models.TokenRefresh
import hr.foi.sis.passwordium.network.NetworkServis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object JwtManager {
    private var algorithm = Algorithm.HMAC256(BuildConfig.JWTKEY)
    private lateinit var jwt: String
    private lateinit var refreshToken: String
    private lateinit var refreshTokenExpiresAt: Date
    private lateinit var userId: String
    private lateinit var username: String
    private lateinit var jwtExpiresAt: Date

    fun decodeJWT(){
        val decodedJWT: DecodedJWT = JWT.require(algorithm).build().verify(jwt)
        userId = decodedJWT.getClaim("id").asString()
        username = decodedJWT.getClaim("unique_name").asString()
        jwtExpiresAt = decodedJWT.expiresAt
    }

    fun saveTokens(body: UserResponse){
        jwt = body.jwt
        refreshToken = body.refreshToken
        refreshTokenExpiresAt = parseRefreshTokenExpirationFromStringToDate(body.refreshTokenExpiresAt)
    }

    fun parseRefreshTokenExpirationFromStringToDate(date: String): Date {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'", Locale("hr", "HR"))
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        Log.i("JWT", inputFormat.parse(date).toString())
        return inputFormat.parse(date)
    }

    fun checkIfJwtExpired(): Boolean {
        val exp = jwtExpiresAt.time / 1000
        val currentTime = System.currentTimeMillis() / 1000
        Log.i("jwt", "jwt expiration:"+exp.toString())
        Log.i("jwt", "current time:"+currentTime.toString())
        return exp < currentTime
    }

    fun checkIfRefreshTokenExpired(): Boolean {
        val refreshTokenExpiresAtTimestamp = refreshTokenExpiresAt.time / 1000
        val currentTime = System.currentTimeMillis() / 1000
        return refreshTokenExpiresAtTimestamp < currentTime
    }

    suspend fun generateNewJwt(): UserResponse? {
        return suspendCoroutine { continuation ->
            val tokenServis = NetworkServis.tokenSerivs
            val tokenRefresh = TokenRefresh(refreshToken)
            val jwtToRefresh = "Bearer $jwt"
            tokenServis.generateNewJwt(jwtToRefresh,tokenRefresh).enqueue(
                object : retrofit2.Callback<UserResponse> {
                    override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                        Log.i("Response", response.code().toString())
                        val body = response.body()
                        if (body != null) {
                            saveTokens(body)
                            decodeJWT()
                            continuation.resume(body)
                        } else {
                            continuation.resume(null)
                        }
                    }

                    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                        Log.i("Response", t.toString())
                        continuation.resume(null)
                    }
                })
        }
    }

    fun giveJwtToken(callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = if (checkIfJwtExpired()) {
                if (checkIfRefreshTokenExpired()) {
                    null // or return some default value
                } else {
                    val response = generateNewJwt()
                    response?.let {
                        // The generateNewJwt has completed, and jwt is updated
                        jwt
                    }
                }
            } else {
                jwt
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }
}