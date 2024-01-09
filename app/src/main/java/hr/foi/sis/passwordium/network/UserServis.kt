package hr.foi.sis.passwordium.network

import hr.foi.sis.passwordium.models.User
import hr.foi.sis.passwordium.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserServis {
    @POST("Users/Login")
    fun login(@Body user: User): Call<UserResponse>
}