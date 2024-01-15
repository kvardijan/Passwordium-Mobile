package hr.foi.sis.passwordium

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.warrenstrange.googleauth.GoogleAuthenticator
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig
import com.warrenstrange.googleauth.GoogleAuthenticatorKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class QRScanner : AppCompatActivity() {
    private lateinit var barcodeView: CompoundBarcodeView
    private lateinit var qrHelp: TextView
    private lateinit var txtTotp: TextView
    private lateinit var btnGeneriraj: Button
    private lateinit var btnSkenirajQR: Button

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscanner)

        barcodeView = findViewById(R.id.cameraPreview)
        qrHelp = findViewById(R.id.txtQRhelp)
        txtTotp = findViewById(R.id.txtTotp)
        btnGeneriraj = findViewById(R.id.btnGenerirajTotp)
        btnSkenirajQR = findViewById(R.id.skenirajQR)

        val providers = Security.getProviders()
        for (provider in providers) {
            println("Provider: ${provider.name}")
            for (service in provider.services) {
                println("  Algorithm: ${service.algorithm}")
            }
        }

        btnGeneriraj.setOnClickListener {
            val totp = generateTotp(getStringFromSharedPreferences(this, "totpKey")!!)
            txtTotp.text = totp.toString()
        }

        btnSkenirajQR.setOnClickListener {
            scanningMode()
        }

        if(!checkIfQrIsScanned()){
            scanningMode()
        }else{
            totpShowingMode()
        }
    }

    fun scanningMode(){
        barcodeView.visibility = View.VISIBLE
        qrHelp.visibility = View.VISIBLE
        txtTotp.visibility = View.GONE
        btnGeneriraj.visibility = View.GONE
        btnSkenirajQR.visibility = View.GONE
        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    fun totpShowingMode(){
        barcodeView.visibility = View.GONE
        qrHelp.visibility = View.GONE
        txtTotp.visibility = View.VISIBLE
        btnGeneriraj.visibility = View.VISIBLE
        btnSkenirajQR.visibility = View.VISIBLE
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Potreban je pristup kameri kako bi skenirali QR kod.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startCamera() {
        barcodeView.decodeSingle { result ->
            result?.let {
                //Toast.makeText(this@QRScanner, it.text, Toast.LENGTH_LONG).show()
                saveStringToSharedPreferences(this, "totpKey", it.text)
                totpShowingMode()
            }
            barcodeView.resume()
        }
    }

    private fun generateTotp(totpKey: String): Int {
        System.setProperty("com.warrenstrange.googleauth.rng.algorithmProvider", "AndroidOpenSSL");
        val gAuth = GoogleAuthenticator()
        return gAuth.getTotpPassword(totpKey)
    }

    override fun onResume() {
        super.onResume()

        if (hasCameraPermission()) {
            barcodeView.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    fun saveStringToSharedPreferences(context: Context, key: String, value: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("totp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringFromSharedPreferences(context: Context, key: String): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("totp", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }

    fun checkIfQrIsScanned(): Boolean{
        return getStringFromSharedPreferences(this, "totpKey") != null
    }
}