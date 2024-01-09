package hr.foi.sis.passwordium

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView

class MainActivity : AppCompatActivity() {
    private lateinit var barcodeView: CompoundBarcodeView

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        barcodeView = findViewById(R.id.cameraPreview)
        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
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
                Toast.makeText(this@MainActivity, it.text, Toast.LENGTH_LONG).show()
            }
            barcodeView.resume()
        }
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
}
