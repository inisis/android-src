package hitrouble.com

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.google.android.cameraview.CameraView
import hitrouble.com.api.PlateApi
import hitrouble.com.db.Record
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import com.github.kittinunf.fuel.Fuel


class MainActivity : AppCompatActivity() {

    private var waitingDialog: ProgressDialog? = null

    private fun lockTake(lock: Boolean) {
        takePicture?.isEnabled = !lock
        if (lock) {
            waitingDialog = ProgressDialog.show(this, "", "上传中", false)
        } else {
            waitingDialog?.dismiss()
        }
    }

    private val callback = object : CameraView.Callback() {
        override fun onPictureTaken(cameraView: CameraView?, data: ByteArray) {
            super.onPictureTaken(cameraView, data)
            val subCode = System.currentTimeMillis() * 1000 + (Math.random() * 1000).toInt()

            val options = BitmapFactory.Options()
            options.inSampleSize = 4
            val bit = BitmapFactory.decodeByteArray(data, 0, data.size, options)
            val stream = ByteArrayOutputStream()
            bit.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            //压缩后的
            val compress = stream.toByteArray()
            stream.close()

            saveRecord(subCode, compress)
            val base64 = Base64.encodeToString(compress, Base64.NO_WRAP)
            val pkt = "{\"busCode\":$subCode,\"base64\":\"$base64\"}"

            val bodyJson = """
                        {
                            "Context": {
                                "SessionId": "test",
                                "Functions": [100, 101,102, 103, 104],
                                "Type": 1
                            },
                            "Image": {
                                "Data": {
                                    "BinData": "$base64"
                                }
                            }
                        }
                        """

            System.out.println(bodyJson)

            Fuel.post("http://117.40.83.179:6666/process/single").body(bodyJson).timeout(5000).response { request, response, result ->
//                waitingDialog = ProgressDialog.show(this@MainActivity, "", String(response.data), false)

                val resp = JSONObject(String(response.data))

//                waitingDialog = ProgressDialog.show(this@MainActivity, "", resp.getJSONObject("Result").toString(), false)

                runOnUiThread {
                    lockTake(false)
                    Toast.makeText(this@MainActivity, "success", Toast.LENGTH_LONG).show()
                }

                if (resp.getJSONObject("Context").getInt("Status") == 200) {
                    val plats = PlateApi.platesInResult(resp)
                    for (p in plats) {
                        Log.e("#", p.plate)
                    }
                    runOnUiThread {
                        if (plats.isEmpty()) {
                            Toast.makeText(this@MainActivity, "未识别", Toast.LENGTH_LONG).show()
                            val box = (application as AppInstance).getBoxStore()?.boxFor(Record::class.java)
                            box?.remove(subCode)
                            return@runOnUiThread
                        }
                        val alertDialog = AlertDialog.Builder(this@MainActivity)
                        alertDialog.setTitle(location?.address)
                        alertDialog.setSingleChoiceItems(plats, 0) { p0, p1 ->
                            p0.dismiss()
                            val box = (application as AppInstance).getBoxStore()?.boxFor(Record::class.java)
                            val rec = box?.get(subCode)
                            if (rec != null) {
                                rec.plateValue = plats[p1].plate
                                box.put(rec)
                            }
                            startActivity(Intent(this@MainActivity, DetailActivity::class.java))
                        }
                        alertDialog.show()
                    }
                }

//                waitingDialog = ProgressDialog.show(this@MainActivity, "", resp.getJSONObject("Context").toString(), false)
//                waitingDialog = ProgressDialog.show(this@MainActivity, "", resp.getJSONObject("Result").toString(), false)
            }

            // plateApi?.sendMessage(pkt)
        }
    }

    fun saveRecord(code: Long, byteArray: ByteArray) {
        val path = saveFile(byteArray, code)
        val record = Record()
        record.busCode = code
        record.plateImage = path
        record.submited = false
        record.address = location?.address
        record.latitude = location?.latitude ?: 0.0
        record.longitude = location?.longitude ?: 0.0
        record.time = System.currentTimeMillis()
        val box = (application as AppInstance).getBoxStore()?.boxFor(Record::class.java)
        box?.put(record)
    }

    fun saveFile(byteArray: ByteArray, subCode: Long): String {
        val dir = "${cacheDir.absoluteFile}${File.separator}images${File.separator}"
        val dirFile = File(dir)
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        val image = "$dir$subCode.jpg"
        val imageFile = File(image)
        imageFile.createNewFile()
        run {
            val fileOutputStream = FileOutputStream(imageFile)
            fileOutputStream.write(byteArray)
            fileOutputStream.close()
        }
        run {
            val fileOutputStream = FileOutputStream("${Environment.getExternalStorageDirectory().absoluteFile}/111.jpg")
            fileOutputStream.write(byteArray)
            fileOutputStream.close()
        }
        return image
    }

    private var location: AMapLocation? = null

    private val listener = AMapLocationListener {
        location = it
        Log.e("#", it.address)
    }

    private fun requestLocation() {
        val client = AMapLocationClient(application)
        val option = AMapLocationClientOption()
        option.isNeedAddress = true
        option.isOnceLocation = false
        option.isLocationCacheEnable = false
        option.isMockEnable = false
        option.interval = 10 * 1000
        option.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        client.setLocationOption(option)
        client.setLocationListener(listener)
        client.startLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestLocation()
        camera?.addCallback(callback)

        takePicture?.setOnClickListener {
            if (location != null) {
                lockTake(true)
                camera?.takePicture()
            } else {
                Toast.makeText(this, "获取精准定位失败", Toast.LENGTH_LONG).show()
            }
        }
        record?.setOnClickListener {
            startActivity(Intent(this, DetailActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            for (i in permissions.indices) {
                if (permissions[i] == Manifest.permission.CAMERA) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        camera?.start()
                    } else {
                        Toast.makeText(this, "未获取拍照权限", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            camera?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        camera?.stop()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        moveTaskToBack(true)
    }
}
