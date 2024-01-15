package hr.foi.sis.passwordium

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.foi.sis.passwordium.managers.FingerprintManager
import hr.foi.sis.passwordium.managers.JwtManager
import hr.foi.sis.passwordium.models.AccountResponse
import hr.foi.sis.passwordium.network.NetworkServis
import retrofit2.Call
import retrofit2.Response

class UserAccountsActivity: AppCompatActivity() {

    private lateinit var btnExit: ImageButton
    private lateinit var btnGenToken: ImageButton
    private lateinit var btnAddNew: ImageButton

    private lateinit var recyclerView : RecyclerView

    private var accountServis = NetworkServis.accountServis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_accounts)

        btnExit = findViewById(R.id.btnExit)
        btnGenToken = findViewById(R.id.btnGenToken)
        btnAddNew = findViewById(R.id.btnAddNew)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        //dohvatiti s backenda (treba implementirati)
        sendGetAccountsRequest()

        val accountItems = listOf(
            AccountItem("Website 1", "Username 1", "123456","www.example1.com"),
            AccountItem("Website 2", "Username 2", "123456",""),
            // Add more items as needed
        )
        /*
        val adapter = AccountAdapter(accountItems,this)
        recyclerView.adapter = adapter
        */
        btnExit.setOnClickListener {
            JwtManager.logout()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnGenToken.setOnClickListener {
            val intent = Intent(this, QRScanner::class.java)
            startActivity(intent)
        }

        btnAddNew.setOnClickListener {
            val intent = Intent(this, AddAccountActivity::class.java)
            startActivity(intent)
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