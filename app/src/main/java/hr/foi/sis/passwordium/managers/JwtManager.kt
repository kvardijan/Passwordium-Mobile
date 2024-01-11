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
import retrofit2.Call
import retrofit2.Response

object JwtManager {
    var algorithm = Algorithm.HMAC256(BuildConfig.JWTKEY)
    lateinit var jwt: String
    lateinit var refreshToken: String
    lateinit var refreshTokenExpiresAt: Date
    lateinit var userId: String
    lateinit var username: String

    fun decodeJWT(){
        val decodedJWT: DecodedJWT = JWT.require(algorithm).build().verify(jwt)
        userId = decodedJWT.getClaim("id").asString()
        username = decodedJWT.getClaim("unique_name").asString()
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
        val decodedJWT: DecodedJWT = JWT.require(algorithm).build().verify(jwt)
        val exp = decodedJWT.getClaim("exp").asLong()
        val currentTimeMilliseconds = System.currentTimeMillis() / 1000
        return exp < currentTimeMilliseconds
    }

    fun checkIfRefreshTokenExpired(): Boolean {
        val refreshTokenExpiresAtTimestamp: Long = refreshTokenExpiresAt.time / 1000
        val currentTimeMilliseconds = System.currentTimeMillis() / 1000
        return refreshTokenExpiresAtTimestamp < currentTimeMilliseconds
    }

    fun generateNewJwt(){
        val tokenServis = NetworkServis.tokenSerivs
        val tokenRefresh = TokenRefresh(refreshToken)
        tokenServis.generateNewJwt(tokenRefresh).enqueue(
            object : retrofit2.Callback<UserResponse>{
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    Log.i("Response", response.code().toString())
                    val body = response.body()
                    if (body != null) {
                        saveTokens(body)
                        decodeJWT()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.i("Response", t.toString())
                }
            })
    }
}