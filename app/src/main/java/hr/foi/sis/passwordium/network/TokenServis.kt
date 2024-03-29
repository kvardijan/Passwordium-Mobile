package hr.foi.sis.passwordium.network

import hr.foi.sis.passwordium.models.TokenRefresh
import hr.foi.sis.passwordium.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TokenServis {
    @POST("Users/TokenRefresh")
    fun generateNewJwt(@Header("Authorization") authorization: String, @Body tokenRefresh: TokenRefresh): Call<UserResponse>
}