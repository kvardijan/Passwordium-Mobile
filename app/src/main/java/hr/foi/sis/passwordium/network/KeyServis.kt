package hr.foi.sis.passwordium.network

import hr.foi.sis.passwordium.models.PublicKey
import hr.foi.sis.passwordium.models.PublicKeyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface KeyServis {
    @POST("Users/PublicKey")
    fun storePublicKey(@Header("Authorization") authorization: String, @Body key: PublicKey): Call<PublicKeyResponse>
}