package hitrouble.com

import android.app.Application
import hitrouble.com.db.MyObjectBox
import io.objectbox.BoxStore

class AppInstance : Application() {

    private var boxStore: BoxStore? = null

    override fun onCreate() {
        super.onCreate()
        boxStore = MyObjectBox.builder().androidContext(this).build()
    }

    fun getBoxStore(): BoxStore? {
        return boxStore
    }
}