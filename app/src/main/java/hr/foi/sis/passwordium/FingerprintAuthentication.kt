package hr.foi.sis.passwordium

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import hr.foi.sis.passwordium.managers.BiometricManager
import hr.foi.sis.passwordium.managers.JwtManager

class FingerprintAuthentication : AppCompatActivity() {
    private lateinit var btnStore: Button // FOR TESTING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint_authentication)

        // FOR TESTING
        val dataToSign = "a"
        btnStore = findViewById(R.id.btnStore)
        btnStore.setOnClickListener {
            //JwtManager.checkIfJwtExpired()
            //JwtManager.checkIfRefreshTokenExpired()
            val signature = BiometricManager.signDataWithPrivateKey(dataToSign)
            Log.i("Signature", signature ?: "Signing failed")

            val isVerified = BiometricManager.verifySignatureWithPublicKey(dataToSign, signature ?: "")
            Log.i("Verification", "Signature verification result: $isVerified")
        }
        // FOR TESTING
    }
}
