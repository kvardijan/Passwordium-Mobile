package hr.foi.sis.passwordium

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import hr.foi.sis.passwordium.managers.FingerprintManager
import hr.foi.sis.passwordium.managers.JwtManager
import hr.foi.sis.passwordium.models.AccountResponse
import hr.foi.sis.passwordium.models.CheckPasswordsRequest
import hr.foi.sis.passwordium.models.CheckPasswordsResponse
import hr.foi.sis.passwordium.models.MessageOnlyResponse
import hr.foi.sis.passwordium.network.NetworkServis
import retrofit2.Call
import retrofit2.Response
import java.security.MessageDigest

class UserAccountsActivity: AppCompatActivity() {

    private lateinit var recyclerView : RecyclerView

    private var accountServis = NetworkServis.accountServis
    private var passwords = listOf<AccountResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_accounts)

        val navigation = findViewById<NavigationBarView>(R.id.bottom_navigation)
        navigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.logout-> {
                    JwtManager.logout()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.password_check -> {
                    getBreachedPasswords()
                    true
                }
                R.id.TOTP-> {
                    val intent = Intent(this, QRScanner::class.java)
                    startActivity(intent)
                    true
                }
                R.id.new_account -> {
                    val intent = Intent(this, AddAccountActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        //dohvatiti s backenda (treba implementirati)
        sendGetAccountsRequest()
    }

    private fun sha1(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val result = digest.digest(input.toByteArray())

        val hexChars = CharArray(result.size * 2)
        for (i in result.indices) {
            val v = result[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v shr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }

        return String(hexChars)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun getBreachedPasswords(){
        val passwordsRequest = mutableListOf<CheckPasswordsRequest>()
        passwords.forEach {
            val hashedPassword = sha1(it.password)
            passwordsRequest += CheckPasswordsRequest(it.id,hashedPassword)
        }
        Log.i("body",passwordsRequest.toString())

        JwtManager.giveJwtToken { jwtToken ->
            if (jwtToken != null) {
                val jwtAuthorization = "Bearer $jwtToken"
                accountServis.checkPassword(jwtAuthorization,passwordsRequest).enqueue(
                    object : retrofit2.Callback<List<CheckPasswordsResponse>>{
                        override fun onResponse(
                            call: Call<List<CheckPasswordsResponse>>,
                            response: Response<List<CheckPasswordsResponse>>
                        ) {
                            val body = response.body() as List<CheckPasswordsResponse>
                            Log.i("body",body.toString())

                            if(body.isEmpty()){
                                showToast("No breached passwords found!! :)")
                                return
                            } else {
                                body.forEach { breachedPassword ->
                                    val account = passwords.find { it.id == breachedPassword.id }
                                    account?.let {
                                        val accountName = it.name
                                        val message = "Password for $accountName has been breached!"
                                        showToast(message)
                                    }
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<List<CheckPasswordsResponse>>,
                            t: Throwable
                        ) {
                            Log.i("response", "Checking passwords was unsuccessful.")
                        }
                    }
                )
            }
            else {
                Log.i("JwtToken", "Token is null")
            }
        }
    }

    fun sendGetAccountsRequest(){
        JwtManager.giveJwtToken { jwtToken ->
            if (jwtToken != null) {
                val jwtAuthorization = "Bearer $jwtToken"
                accountServis.getAccounts(jwtAuthorization).enqueue(
                    object : retrofit2.Callback<List<AccountResponse>>{
                        override fun onResponse(
                            call: Call<List<AccountResponse>>,
                            response: Response<List<AccountResponse>>
                        ) {
                            val body = response.body() as List<AccountResponse>
                            Log.i("body",body.toString())
                            passwords = body
                            val adapter = AccountAdapter(body,this@UserAccountsActivity)
                            recyclerView.adapter = adapter
                        }

                        override fun onFailure(
                            call: Call<List<AccountResponse>>,
                            t: Throwable
                        ) {
                            Log.i("response", "Fetching user accounts was unsuccessful.")
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