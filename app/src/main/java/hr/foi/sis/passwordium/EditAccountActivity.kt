package hr.foi.sis.passwordium

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import hr.foi.sis.passwordium.managers.CryptingManager
import hr.foi.sis.passwordium.managers.JwtManager
import hr.foi.sis.passwordium.models.Account
import hr.foi.sis.passwordium.models.EditAccountBody
import hr.foi.sis.passwordium.models.MessageOnlyResponse
import hr.foi.sis.passwordium.network.NetworkServis
import me.gosimple.nbvcxz.resources.Generator
import retrofit2.Call
import retrofit2.Response
import kotlin.reflect.typeOf

class EditAccountActivity: AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var url: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var btnGeneratePassword: Button
    private lateinit var btnDelete: Button
    private lateinit var btnSave: Button
    private lateinit var btnBackButton: ImageButton

    private var accountId : Int ?= null

    private var accountServis = NetworkServis.accountServis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_account)

        name = findViewById(R.id.etWebsiteName)
        url = findViewById(R.id.etUrl)
        username = findViewById(R.id.etUsername)
        password = findViewById(R.id.etPassword)
        btnGeneratePassword = findViewById(R.id.btnGeneratePassword)
        btnBackButton = findViewById(R.id.backArrow)
        btnDelete = findViewById(R.id.btnDeleteAccount)
        btnSave = findViewById(R.id.btnSaveChanges)

        if(intent.hasExtra("selectedAccountWebsiteName")){
            name.setText(intent.getStringExtra("selectedAccountWebsiteName"))
        }
        if(intent.hasExtra("selectedAccountUsername")){
            username.setText(intent.getStringExtra("selectedAccountUsername"))
        }
        if(intent.hasExtra("selectedAccountUrl")){
            url.setText(intent.getStringExtra("selectedAccountUrl"))
        }
        if(intent.hasExtra("selectedAccountPassword")){
            password.setText(intent.getStringExtra("selectedAccountPassword"))
        }
        if(intent.hasExtra("selectedAccountId")){
            accountId = intent.getStringExtra("selectedAccountId")?.toInt()
        }

        btnGeneratePassword.setOnClickListener {
            generatePassword()
        }

        btnBackButton.setOnClickListener {
            val intent = Intent(this,UserAccountsActivity::class.java)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            deleteAccount()
        }

        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun deleteAccount(){
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Uklanjanje računa")
            .setMessage("Jeste li sigurni da želite obrisati račun?")

        builder.setPositiveButton("Da") { _, _ ->
            accountId?.let { sendDeleteAccountRequest(it) }
        }

        builder.setNegativeButton("Ne") { _, _ ->
            // samo zatvori alert
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun saveChanges(){
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
        accountId?.let {
            val newAccount = Account(nameContent, urlContent, usernameContent, passwordContent)
            newAccount.password = CryptingManager.encryptor(this, newAccount)
            val account = EditAccountBody(it,newAccount.name, newAccount.url ,newAccount.username, newAccount.password)
            sendEditAccountRequest(account)
        }

    }

    private fun generatePassword() {
        val lozinka = Generator.generatePassphrase("!-?", 5)
        password.setText(lozinka)
    }

    fun sendEditAccountRequest(account: EditAccountBody){
        JwtManager.giveJwtToken { jwtToken ->
            if (jwtToken != null) {
                val jwtAuthorization = "Bearer $jwtToken"
                accountServis.updateAccount(jwtAuthorization,account).enqueue(
                    object : retrofit2.Callback<MessageOnlyResponse>{
                        override fun onResponse(
                            call: Call<MessageOnlyResponse>,
                            response: Response<MessageOnlyResponse>
                        ) {
                            Log.i("bodyCode",response.code().toString())
                            Log.i("body",response.body().toString())

                            val intent = Intent(this@EditAccountActivity,UserAccountsActivity::class.java)
                            startActivity(intent)
                        }

                        override fun onFailure(
                            call: Call<MessageOnlyResponse>,
                            t: Throwable
                        ) {
                            Log.i("response", "Updating the account was unsuccessful.")
                        }
                    }
                )
            }
            else {
                Log.i("JwtToken", "Token is null")
            }
        }
    }

    fun sendDeleteAccountRequest(id : Int){
        JwtManager.giveJwtToken { jwtToken ->
            if (jwtToken != null) {
                val jwtAuthorization = "Bearer $jwtToken"
                accountServis.deleteAccount(jwtAuthorization,id).enqueue(
                    object : retrofit2.Callback<MessageOnlyResponse>{
                        override fun onResponse(
                            call: Call<MessageOnlyResponse>,
                            response: Response<MessageOnlyResponse>
                        ) {
                            Log.i("bodyCode",response.code().toString())
                            Log.i("body",response.body().toString())

                            val intent = Intent(this@EditAccountActivity,UserAccountsActivity::class.java)
                            startActivity(intent)
                        }

                        override fun onFailure(
                            call: Call<MessageOnlyResponse>,
                            t: Throwable
                        ) {
                            Log.i("response", "Updating the account was unsuccessful.")
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