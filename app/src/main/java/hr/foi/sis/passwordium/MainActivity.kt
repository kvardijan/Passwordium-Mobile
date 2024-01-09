package hr.foi.sis.passwordium

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var btnPrijava: Button
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

    }
}