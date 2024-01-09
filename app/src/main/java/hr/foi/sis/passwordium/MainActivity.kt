package hr.foi.sis.passwordium

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import hr.foi.sis.passwordium.models.User
import hr.foi.sis.passwordium.models.UserResponse
import hr.foi.sis.passwordium.network.NetworkServis
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var btnPrijava: Button
    private var userServis = NetworkServis.userServis
    private lateinit var testBtn: Button // FOR TESTING
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPrijava = findViewById(R.id.btnPrijava)
        btnPrijava.setOnClickListener{
            sendLoginRequest()
        }

        // FOR TESTING
        testBtn = findViewById(R.id.testBtn)
        testBtn.setOnClickListener{
            val intent = Intent(this@MainActivity, QRScanner::class.java)
            startActivity(intent)
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
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.i("Response", t.toString())
                }

            }
        )
    }
}