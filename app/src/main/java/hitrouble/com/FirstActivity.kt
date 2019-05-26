package hitrouble.com

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity

class FirstActivity : AppCompatActivity() {

    private val permissions =
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    private fun checkPermission() {
        if (!isPermissionPass()) {
            ActivityCompat.requestPermissions(this, permissions, 10001)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun isPermissionPass(): Boolean {
        var permissionPass = true
        for (per in permissions) {
            if (ActivityCompat.checkSelfPermission(this, per) != PackageManager.PERMISSION_GRANTED) {
                permissionPass = false
                break
            }
        }
        return permissionPass
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10001) {
            checkPermission()
        }
    }
}
