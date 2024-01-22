package hr.foi.sis.passwordium.models

data class Account(
    val name: String,
    val url: String,
    val username: String,
    var password: String
)