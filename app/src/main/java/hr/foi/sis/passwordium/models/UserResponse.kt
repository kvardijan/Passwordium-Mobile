package hr.foi.sis.passwordium.models

data class UserResponse(
    val jwt: String,
    val refreshToken: String,
    val refreshTokenExpiresAt: String
)
