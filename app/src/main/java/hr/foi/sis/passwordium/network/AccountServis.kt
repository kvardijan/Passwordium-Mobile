package hr.foi.sis.passwordium.network

import hr.foi.sis.passwordium.models.Account
import hr.foi.sis.passwordium.models.AccountResponse
import hr.foi.sis.passwordium.models.CheckPasswordsRequest
import hr.foi.sis.passwordium.models.CheckPasswordsResponse
import hr.foi.sis.passwordium.models.EditAccountBody
import hr.foi.sis.passwordium.models.MessageOnlyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AccountServis {
    @GET("Accounts")
    fun getAccounts(@Header("Authorization") authorization: String): Call<List<AccountResponse>>

    @POST("Accounts")
    fun createAccount(@Header("Authorization") authorization: String, @Body account: Account): Call<MessageOnlyResponse>

    @PUT("Accounts")
    fun updateAccount(@Header("Authorization") authorization: String, @Body account: EditAccountBody): Call<MessageOnlyResponse>

    @DELETE("Accounts/{id}")
    fun deleteAccount(@Header("Authorization") authorization: String, @Path("id") accountId: Int): Call<MessageOnlyResponse>

    @POST("Accounts/CheckPasswords")
    fun checkPassword(@Header("Authorization") authorization: String, @Body checkPasswordsRequest: List<CheckPasswordsRequest>): Call<List<CheckPasswordsResponse>>
}