package hitrouble.com

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import hitrouble.com.db.Record
import kotlinx.android.synthetic.main.activity_detail.*
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    var dataSet: List<Record>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val box = (application as AppInstance).getBoxStore()?.boxFor(Record::class.java)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        dataSet = box?.all
        dataSet = dataSet?.reversed()
        recyclerView?.adapter = recordAdapter
        toolBar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private val recordAdapter = RecordAdapter()

    inner class RecordAdapter : RecyclerView.Adapter<RecordAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_record, p0, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return dataSet?.size ?: 0
        }

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            val timeStamp = dataSet?.get(p1)?.time ?: 0
            p0.time?.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(timeStamp))
            p0.address?.text = dataSet?.get(p1)?.address
            p0.plate?.text = dataSet?.get(p1)?.plateValue ?: "未识别"
            Glide.with(this@DetailActivity).load(dataSet?.get(p1)?.plateImage).into(p0.imageView!!)
        }

        inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
            var imageView: ImageView? = null
            var plate: TextView? = null
            var address: TextView? = null
            var time: TextView? = null

            init {
                imageView = item.findViewById(R.id.image)
                plate = item.findViewById(R.id.plate)
                address = item.findViewById(R.id.address)
                time = item.findViewById(R.id.time)
            }
        }
    }
}
