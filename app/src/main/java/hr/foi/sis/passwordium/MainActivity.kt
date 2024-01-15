package hr.foi.sis.passwordium

import android.content.Intent
import android.os.Bundle
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
import hr.foi.sis.passwordium.models.Challenge
import hr.foi.sis.passwordium.models.ChallengeResponse
import hr.foi.sis.passwordium.models.ChallengeVerification
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
    //private lateinit var btnQR: Button //TEST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TEST
        //btnQR = findViewById(R.id.btnQR)
        //btnQR.setOnClickListener {
        //    val intent = Intent(this, QRScanner::class.java)
        //    startActivity(intent)
        //}
        //TEST
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
                //tu ide kad fingerprint dobar
                signServerChallenge()
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
    }

    fun signServerChallenge(){
        val challenge = Challenge(FingerprintManager.getPublicKey()!!)
        Log.i("challenge", "key za get challenge:"+challenge.publicKey)
        keyServis.getChallenge(challenge).enqueue(
            object : retrofit2.Callback<ChallengeResponse>{
                override fun onResponse(
                    call: Call<ChallengeResponse>,
                    response: Response<ChallengeResponse>
                ) {
                    val body = response.body()
                    Log.i("challenge", "code"+response.code().toString())
                    if (body != null){
                        Log.i("challenge", body.message)
                        val signature = FingerprintManager.signDataWithPrivateKey(body.message)
                        val challengeVerification = ChallengeVerification(signature!!, FingerprintManager.getPublicKey()!!)

                        keyServis.verifyChallenge(challengeVerification).enqueue(
                            object : retrofit2.Callback<UserResponse>{
                                override fun onResponse(
                                    call: Call<UserResponse>,
                                    response: Response<UserResponse>
                                ) {
                                    val verificationBody = response.body()
                                    Log.i("challenge", "challenge verification respose code:"+response.code())
                                    if (verificationBody != null){
                                        JwtManager.saveTokens(verificationBody)
                                        JwtManager.decodeJWT()
                                        //TODO: REROUTE TO MAIN PASSWORD VIEW
                                        val intent = Intent(this@MainActivity,UserAccountsActivity::class.java)
                                        startActivity(intent)
                                    }
                                }

                                override fun onFailure(
                                    call: Call<UserResponse>,
                                    t: Throwable
                                ) {
                                    Log.i("challenge", "challenge verification failed")
                                }
                            }
                        )
                    }
                }

                override fun onFailure(call: Call<ChallengeResponse>, t: Throwable) {
                    Log.i("challenge", "challenge not received")
                }
            }
        )
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
                                Log.i("registerKey", "key koji se sprema u bazu:"+FingerprintManager.getPublicKey())
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
                                            Toast.makeText(this@MainActivity, "Omogućena prijava otiskom prsta", Toast.LENGTH_LONG)
                                                .show()
                                            isFingerprintLoginEnabled = true
                                            btnPrijava.text = getString(R.string.prijavi_me)
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
                        //TODO: REROUTE TO MAIN PASSWORD VIEW
                        val intent = Intent(this@MainActivity,UserAccountsActivity::class.java)
                        startActivity(intent)
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