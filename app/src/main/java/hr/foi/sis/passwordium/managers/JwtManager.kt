package hr.foi.sis.passwordium.managers

import android.content.pm.ApplicationInfo
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import hr.foi.sis.passwordium.models.UserResponse
import java.util.TimeZone

object JwtManager {
    var algorithm = Algorithm.HMAC256("")
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
        refreshTokenExpiresAt = parseExpirationFromStringToDate(body.refreshTokenExpiresAt)
    }

    fun parseExpirationFromStringToDate(date: String): Date {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'", Locale("hr", "HR"))
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        Log.i("JWT", inputFormat.parse(date).toString())
        return inputFormat.parse(date)
    }

}