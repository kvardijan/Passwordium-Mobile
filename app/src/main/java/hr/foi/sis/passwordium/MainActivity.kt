package hr.foi.sis.passwordium

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import hr.foi.sis.passwordium.managers.FingerprintManager
import hr.foi.sis.passwordium.managers.JwtManager
import hr.foi.sis.passwordium.models.PublicKey
import hr.foi.sis.passwordium.models.PublicKeyResponse
import hr.foi.sis.passwordium.models.User
import hr.foi.sis.passwordium.models.UserResponse
import hr.foi.sis.passwordium.network.NetworkServis
import org.bouncycastle.jce.provider.BouncyCastleProvider
import retrofit2.Call
import retrofit2.Response
import java.security.Security


class MainActivity : AppCompatActivity() {
    private lateinit var btnPrijava: Button
    private lateinit var btnPrijavaFingerprint: Button
    private var userServis = NetworkServis.userServis
    private var keyServis = NetworkServis.keySerivs
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var isFingerprintLoginEnabled: Boolean = false
    private val secProvider = "BC"
    private lateinit var testBtn: Button // FOR TESTING
    private lateinit var testBtn3: Button // FOR TESTING
    private lateinit var testBtn4: Button // FOR TESTING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Security.getProvider(secProvider) == null) {
            Security.addProvider(BouncyCastleProvider())
        }

        btnPrijava = findViewById(R.id.btnPrijava)
        btnPrijavaFingerprint = findViewById(R.id.btnPrijavaFingerprint)

        if (checkIfPublicKeyExists()){
            isFingerprintLoginEnabled = true
            btnPrijava.text = getString(R.string.prijavi_me)
        }

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.i("bio", "Biometric authentication is available")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.i("bio", "This device doesn't support biometric authentication")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.i("bio", "Biometric authentication is currently unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Log.i("bio", "No biometric credentials are enrolled")
        }

        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.i("bio", "Authentication error: " + errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.i("bio", "AUTHENTICATION SUCCESS")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.i("bio", "AUTHENTICATION FAIL")
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        btnPrijava.setOnClickListener{
            if (isFingerprintLoginEnabled){
                sendLoginRequest()
            }else{
                registerFingerprintForLogin()
            }
        }

        btnPrijavaFingerprint.setOnClickListener {
            if (isFingerprintLoginEnabled){
                biometricPrompt.authenticate(promptInfo)
            }else{
                Toast.makeText(
                    this,
                    "Prijava otiskom prsta još nije postavljena.",
                    Toast.LENGTH_LONG)
                    .show()
            }
        }

        // FOR TESTING
        testBtn = findViewById(R.id.testBtn)
        testBtn.setOnClickListener{
            val intent = Intent(this@MainActivity, QRScanner::class.java)
            startActivity(intent)
        }
        testBtn4 = findViewById(R.id.testBtn4)
        testBtn4.setOnClickListener{
            val a = FingerprintManager.getPublicKey()
            Log.i("a", a.toString())
            val signa = FingerprintManager.signDataWithPrivateKey("a")
            val bo = FingerprintManager.verifySignatureWithPublicKey("a",signa!!)
            Log.i("a", bo.toString())
        }
        testBtn3 = findViewById(R.id.testBtn3)
        testBtn3.setOnClickListener{
            JwtManager.giveJwtToken { jwtToken ->
                if (jwtToken != null) {
                    // use jwt
                    Log.i("jwt", "ovo je jwt:"+jwtToken)
                } else {
                    // Go back to login screen
                    Log.i("JwtToken", "Token is null")
                }
            }

        }
        //val signature = FingerprintManager.signDataWithPrivateKey(dataToSign)
        //Log.i("Signature", signature ?: "Signing failed")

        //val isVerified = FingerprintManager.verifySignatureWithPublicKey(dataToSign, signature ?: "")
        //Log.i("Verification", "Signature verification result: $isVerified")
        // FOR TESTING
    }

    fun registerFingerprintForLogin(){
        val txtKorime = findViewById<EditText>(R.id.korimeTxt).text.toString()
        val txtPassword = findViewById<EditText>(R.id.passwordTxt).text.toString()
        val user = User(txtKorime, txtPassword)

        userServis.login(user).enqueue(
            object : retrofit2.Callback<UserResponse>{
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    Log.i("Response", response.code().toString())
                    val body = response.body()
                    if (body != null) {
                        JwtManager.saveTokens(body)
                        JwtManager.decodeJWT()

                        JwtManager.giveJwtToken { jwtToken ->
                            if (jwtToken != null) {
                                val jwtAuthorization = "Bearer $jwtToken"
                                Log.i("response", "ovo je token koji saljem: "+jwtAuthorization)
                                val publicKey = PublicKey(FingerprintManager.getPublicKey()!!)
                                keyServis.storePublicKey(jwtAuthorization, publicKey).enqueue(
                                    object : retrofit2.Callback<PublicKeyResponse>{
                                        override fun onResponse(
                                            call: Call<PublicKeyResponse>,
                                            response: Response<PublicKeyResponse>
                                        ) {
                                            Log.i("response", "SUCCESSfully saved public key")
                                            val test = response.code()
                                            Log.i("response", "ovo je response code: "+test)
                                            JwtManager.logout()
                                            Toast.makeText(this@MainActivity, "Login with fingerprint", Toast.LENGTH_LONG)
                                                .show()
                                        }

                                        override fun onFailure(
                                            call: Call<PublicKeyResponse>,
                                            t: Throwable
                                        ) {
                                            Log.i("response", "Storing of key was not successful")
                                        }
                                    }
                                )
                            } else {
                                Log.i("JwtToken", "Token is null")
                            }
                        }

                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.i("Response", t.toString())
                }

            }
        )
    }

    fun sendLoginRequest(){
        val txtKorime = findViewById<EditText>(R.id.korimeTxt).text.toString()
        val txtPassword = findViewById<EditText>(R.id.passwordTxt).text.toString()
        val user = User(txtKorime, txtPassword)

        userServis.login(user).enqueue(
            object : retrofit2.Callback<UserResponse>{
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    Log.i("Response", response.code().toString())
                    val body = response.body()
                    if (body != null) {
                        JwtManager.saveTokens(body)
                        JwtManager.decodeJWT()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.i("Response", t.toString())
                }

            }
        )
    }

    fun checkIfPublicKeyExists(): Boolean{
        val check = FingerprintManager.doesKeyPairExist()
        Log.i("a", "dal postoje: " + check.toString())
        return check
    }
}