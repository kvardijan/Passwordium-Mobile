package hr.foi.sis.passwordium

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import hr.foi.sis.passwordium.managers.JwtManager
import hr.foi.sis.passwordium.models.Account
import hr.foi.sis.passwordium.models.AccountResponse
import hr.foi.sis.passwordium.models.MessageOnlyResponse
import hr.foi.sis.passwordium.models.User
import hr.foi.sis.passwordium.network.NetworkServis
import me.gosimple.nbvcxz.resources.Generator
import retrofit2.Call
import retrofit2.Response

class AddAccountActivity: AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var url: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var btnGeneratePassword: Button
    private lateinit var btnCreateAccount: Button
    private lateinit var btnBackButton: ImageButton

    private var accountServis = NetworkServis.accountServis
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_account)

        name = findViewById(R.id.etWebsiteName)
        url = findViewById(R.id.etUrl)
        username = findViewById(R.id.etUsername)
        password = findViewById(R.id.etPassword)
        btnGeneratePassword = findViewById(R.id.btnGeneratePassword)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        btnBackButton = findViewById(R.id.backArrow)

        btnGeneratePassword.setOnClickListener {
            generatePassword()
        }

        btnCreateAccount.setOnClickListener {
            createAccount()
        }

        btnBackButton.setOnClickListener {
            val intent = Intent(this,UserAccountsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createAccount(){
        var nameContent: String = name.text.toString().trim()
        var urlContent: String = url.text.toString().trim()
        var usernameContent: String = username.text.toString().trim()
        var passwordContent: String = password.text.toString().trim()

        if(nameContent.isEmpty()){
            name.error = "Potrebno je upisati naziv web stranice!"
            return
        }
        if(usernameContent.isEmpty()){
            username.error = "Potrebno je upisati korisničko ime!"
            return
        }
        if(passwordContent.isEmpty()){
            password.error = "Potrebno je upisati/generirati lozinku!"
            return
        }
        val newAccount = Account(nameContent, urlContent,usernameContent,passwordContent)
        sendAddAccountRequest(newAccount)
    //poslati post request, ako je uspjesno prebaciti usera na MainActivity
    }

    private fun generatePassword() {
        val lozinka = Generator.generatePassphrase("!-?", 5)
        password.setText(lozinka)
    }

    fun sendAddAccountRequest(account: Account){
        JwtManager.giveJwtToken { jwtToken ->
            if (jwtToken != null) {
                val jwtAuthorization = "Bearer $jwtToken"
                accountServis.createAccount(jwtAuthorization,account).enqueue(
                    object : retrofit2.Callback<MessageOnlyResponse>{
                        override fun onResponse(
                            call: Call<MessageOnlyResponse>,
                            response: Response<MessageOnlyResponse>
                        ) {
                            Log.i("bodyCode",response.code().toString())
                            Log.i("body",response.body().toString())

                            val intent = Intent(this@AddAccountActivity,UserAccountsActivity::class.java)
                            startActivity(intent)
                        }

                        override fun onFailure(
                            call: Call<MessageOnlyResponse>,
                            t: Throwable
                        ) {
                            Log.i("response", "Adding new account was unsuccessful.")
                        }
                    }
                )
            }
            else {
                Log.i("JwtToken", "Token is null")
            }
        }
    }
}