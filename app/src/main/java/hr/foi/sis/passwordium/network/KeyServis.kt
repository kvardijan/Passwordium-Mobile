package hr.foi.sis.passwordium.network

import hr.foi.sis.passwordium.models.Challenge
import hr.foi.sis.passwordium.models.ChallengeResponse
import hr.foi.sis.passwordium.models.ChallengeVerification
import hr.foi.sis.passwordium.models.PublicKey
import hr.foi.sis.passwordium.models.PublicKeyResponse
import hr.foi.sis.passwordium.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface KeyServis {
    @POST("Users/PublicKey")
    fun storePublicKey(@Header("Authorization") authorization: String, @Body key: PublicKey): Call<PublicKeyResponse>

    @POST("Users/Challenge")
    fun getChallenge(@Body challenge: Challenge): Call<ChallengeResponse>

    @POST("Users/VerifyChallenge")
    fun verifyChallenge(@Body challengeVerification: ChallengeVerification): Call<UserResponse>
}