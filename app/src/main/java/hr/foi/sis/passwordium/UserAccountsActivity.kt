package hr.foi.sis.passwordium

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import hr.foi.sis.passwordium.managers.FingerprintManager
import hr.foi.sis.passwordium.managers.JwtManager
import hr.foi.sis.passwordium.models.AccountResponse
import hr.foi.sis.passwordium.network.NetworkServis
import retrofit2.Call
import retrofit2.Response

class UserAccountsActivity: AppCompatActivity() {

    private lateinit var recyclerView : RecyclerView

    private var accountServis = NetworkServis.accountServis

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
                    // Respond to navigation item 2 click
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
                            //dobiveni podaci, sada ih treba pohraniti i prikazati na ekranu
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