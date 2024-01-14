package hr.foi.sis.passwordium.models

data class EditAccountBody(
    val id: Int,
    val name: String,
    val url: String,
    val username: String,
    val password: String
)