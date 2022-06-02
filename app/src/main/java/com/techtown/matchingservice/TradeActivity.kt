package com.techtown.matchingservice

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.DeliveryDTO
import com.techtown.matchingservice.util.AlarmReceiver
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TradeActivity : AppCompatActivity() {
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null
    var pid : String? = null
    var item_p = ContentDTO()
    var item_d = DeliveryDTO()
    var cycle : Int? = null
    var contentDTO = ContentDTO()
    val db = Firebase.firestore
    val docRef = db.collection("images")
    val delRef = db.collection("delivery")
    var firestore : FirebaseFirestore? = null

    //var items = ArrayList<Triple<Int, Triple<String, Long, Int>, String>>()
    //var items = ArrayList<Triple<Int, String, Long>>()
    var items = ArrayList<Triple<Triple<Int, Int, Int>, String, Long>>()

    val intent_p = Intent(this, Product::class.java)
    val intent_d = Intent(this, Delivery::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trade)

        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = findViewById(R.id.list_recyclerview)

        firestore = FirebaseFirestore.getInstance()

        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = RecyclerViewAdapter()
        recyclerView?.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        items.clear()
        docRef.get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    item_p = document.toObject(ContentDTO::class.java)
                    if(item_p.uid == uid){
                        items.add(Triple( Triple(1, item_p.cycle, item_p.button), document.id, item_p.timestamp) as Triple<Triple<Int,Int,Int>, String, Long>)
                        //items.add(Triple(1,Triple(item_p.product, item_p.timestamp, item_p.price / item_p.ParticipationTotal),item_p.imageUrl.toString()) as Triple<Int, Triple<String, Long, Int>, String>)
                    }
                }
                delRef.get()
                    .addOnSuccessListener { documents ->
                        for(document in documents){
                            item_d = document.toObject(DeliveryDTO::class.java)
                            if(item_d.delivery_uid == uid){
                                if(item_d.delivery == true){
                                    items.add(Triple(Triple(2, 0, 0), document.id, item_d.delivery_timestamp) as Triple<Triple<Int, Int, Int>, String, Long>)
                                    //items.add(Triple(2,Triple(item_d.store, item_d.delivery_timestamp, null),item_d.imageURL.toString()) as Triple<Int, Triple<String, Long, Int>, String>)
                                } else if(item_d.delivery == false){
                                    items.add(Triple(Triple(3,0,0), document.id, item_d.delivery_timestamp) as Triple<Triple<Int, Int,Int>, String, Long>)
                                    //items.add(Triple(3,Triple(item_d.store, item_d.delivery_timestamp, null),item_d.imageURL.toString()) as Triple<Int, Triple<String, Long, Int>, String>)
                                }
                            }
                        }
                        items.sortBy { it.third }
                        items.reverse()
                        recyclerView!!.adapter!!.notifyDataSetChanged()
                    }
            }

        val finish = findViewById<Button>(R.id.button46)
        finish.setOnClickListener {
            finish()
        }
    }
    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ListViewHolder>(){
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerViewAdapter.ListViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.trade_product_item, parent, false)
            return ListViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewAdapter.ListViewHolder, position: Int) {
            var productitem = ContentDTO()
            var deliveryitem = DeliveryDTO()
            if(items[position].first.first == 1){
                if(items[position].first.third == 1){
                    holder.btn_cp.isEnabled = false
                }
                docRef.get()
                    .addOnSuccessListener { documents ->
                        for(document in documents){
                            if(items[position].second == document.id){
                                productitem = document.toObject(ContentDTO::class.java)
                                Glide.with(holder.itemView.context)
                                    .load(productitem.imageUrl)
                                    .apply(RequestOptions().circleCrop())
                                    .into(holder.img)
                                holder.title.text = productitem.product
                                val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
                                holder.date.text = dateFormat.format(Date(items[position].third))
                                var price : Int = productitem.price / productitem.ParticipationTotal!!
                                holder.price.text = price.toString() + " 원"
                            }
                        }
                    }
                holder.btn_cp.setOnClickListener{
                    pid = items[position].second
                    cycle = Integer.parseInt(items[position].first.second.toString())
                    complete(position)
                }

            } else{
                delRef.get()
                    .addOnSuccessListener { documents ->
                        for(document in documents){
                            if(items[position].second == document.id){
                                deliveryitem = document.toObject(DeliveryDTO::class.java)
                                Glide.with(holder.itemView.context)
                                    .load(deliveryitem.imageURL)
                                    .apply(RequestOptions().circleCrop())
                                    .into(holder.img)
                                holder.title.text = deliveryitem.store
                                val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
                                holder.date.text = dateFormat.format(Date(items[position].third))
                                holder.price.text = ""
                                holder.textv.visibility = View.INVISIBLE
                                holder.btn_cp.visibility = View.INVISIBLE
                            }
                        }
                    }
            }

            holder.card.setOnClickListener {
                if (items[position].first.first == 1){
                    //intent_p.putExtra("productid", items[position].second)
                    //startActivity(intent_p)
                    Intent(applicationContext, Product::class.java).apply {
                        putExtra("productid", items[position].second)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }.run {applicationContext?.startActivity(this)}

                }
            }
        }

        inner class ListViewHolder(view : View) : RecyclerView.ViewHolder(view){
            val img : ImageView = view.findViewById(R.id.list_img)
            val title : TextView = view.findViewById(R.id.list_name)
            val date : TextView = view.findViewById(R.id.rdate)
            val price : TextView = view.findViewById(R.id.list_price)
            val textv : TextView = view.findViewById(R.id.textView3)
            val card : CardView = view.findViewById(R.id.list_card)
            val btn_cp : Button = view.findViewById(R.id.button_complete)
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

    fun setAlarm(productid:String) {
        val calender = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 30)
        }

        calender.add(Calendar.DATE, cycle!!-3)
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        Log.e("날짜", "current: ${df.format(calender.time)}")

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("id",pid)
        intent.putExtra("productid", productid)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            (System.currentTimeMillis()).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        ) // 있으면 새로 만든거로 업데이트

        alarmManager.setInexactRepeating( // 정시에 반복
            AlarmManager.RTC_WAKEUP, // RTC_WAKEUP : 실제 시간 기준으로 wakeup , ELAPSED_REALTIME_WAKEUP : 부팅 시간 기준으로 wakeup
            calender.timeInMillis, // 언제 알람이 발동할지.
            AlarmManager.INTERVAL_DAY, // 하루에 한번씩.
            pendingIntent
        )
    }

    fun complete(position:Int){
        val calender = Calendar.getInstance()
        calender.add(Calendar.DATE, cycle!!)
        val builder = AlertDialog.Builder(this)
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        builder.setTitle("거래 완료")
            .setMessage("거래 완료 하시겠습니까?\n 다음 거래 날짜는 ${df.format(calender.time)} 입니다.\n다음 거래를 원하지 않을 시 해당 게시물을 삭제해주세요!")
            .setPositiveButton("예",
                DialogInterface.OnClickListener{ dialog, id->
                    var tsDoc = firestore?.collection("images")?.document(items[position].second.toString())
                    firestore?.runTransaction { transition ->
                        contentDTO = transition.get(tsDoc!!).toObject(ContentDTO::class.java)!!
                        contentDTO.button = 1
                        transition.set(tsDoc, contentDTO)
                    }
                    setAlarm(items[position].second.toString())
                    finish()
                })
            .setNegativeButton("아니요",
                DialogInterface.OnClickListener{ dialog, id->
                })
        builder.show()

    }
}