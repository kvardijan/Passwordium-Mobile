package hr.foi.sis.passwordium.models

data class AccountResponse(
    val id: Int,
    val name: String,
    val url: String,
    val username: String,
    val password: String
)