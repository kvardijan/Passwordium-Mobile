package hr.foi.sis.passwordium

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import hr.foi.sis.passwordium.managers.BiometricManager
import hr.foi.sis.passwordium.managers.JwtManager
import hr.foi.sis.passwordium.models.User
import hr.foi.sis.passwordium.models.UserResponse
import hr.foi.sis.passwordium.network.NetworkServis
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var btnPrijava: Button
    private var userServis = NetworkServis.userServis
    private lateinit var testBtn: Button // FOR TESTING
    private lateinit var testBtn2: Button // FOR TESTING
    private lateinit var testBtn3: Button // FOR TESTING
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPrijava = findViewById(R.id.btnPrijava)
        btnPrijava.setOnClickListener{
            sendLoginRequest()
        }
        if (checkIfPublicKeyExists()){
            //redirect to fingerprint
            val intent = Intent(this@MainActivity, FingerprintAuthentication::class.java)
            startActivity(intent)
        }else{
            //stay and login
        }

        // FOR TESTING
        testBtn = findViewById(R.id.testBtn)
        testBtn.setOnClickListener{
            val intent = Intent(this@MainActivity, QRScanner::class.java)
            startActivity(intent)
        }
        testBtn2 = findViewById(R.id.testBtn2)
        testBtn2.setOnClickListener{
            val intent = Intent(this@MainActivity, FingerprintAuthentication::class.java)
            startActivity(intent)
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
        // FOR TESTING
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
        val publicKey = BiometricManager.getPublicKey()
        return publicKey != null
    }
}