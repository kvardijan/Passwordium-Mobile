package hr.foi.sis.passwordium.models

data class ChallengeVerification(
    val signature: String,
    val publicKey: String
)
