package com.techtown.matchingservice

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.model.ChatModel
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.util.AlarmReceiver
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GroupActivity : AppCompatActivity() {
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null
    //lateinit var idlist : ArrayList<String>
    var pid :String? = null
    var item = ContentDTO()
    var cycle : Int? = null
    val db = Firebase.firestore
    val docRef = db.collection("images")
    var firestore : FirebaseFirestore? = null

    var groups = ArrayList<Pair<ContentDTO,String>>()

    private var databse = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val roomsRef = databse.getReference("chatrooms")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group)

        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = findViewById(R.id.group_recycler)

        firestore = FirebaseFirestore.getInstance()

        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = RecyclerViewAdapter()
        recyclerView?.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val finish = findViewById<Button>(R.id.button47)
        finish.setOnClickListener {
            finish()
        }
    }
    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.GroupViewHolder>(){

        init {
            groups.clear()
            docRef.get()
                .addOnSuccessListener { documents ->
                    for(document in documents){
                        item = document.toObject(ContentDTO::class.java)
                        if(item.Participation.get(uid) == true){
                            //Toast.makeText(this@GroupActivity, item.product.toString(), Toast.LENGTH_SHORT).show()
                            groups.add(Pair(item, document.id))
                        }
                        //idlist.add(document.id)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerViewAdapter.GroupViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.group_product_item, parent, false)
            return GroupViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewAdapter.GroupViewHolder, @SuppressLint("RecyclerView") position: Int) {
            if(groups[position].first.uid.toString() == uid.toString()){
                holder.btn_drop.visibility = View.INVISIBLE
                holder.btn_cp.visibility = View.VISIBLE
            }
            //pid = idlist[position]

            holder.tv_product.text = groups[position].first.product
            holder.tv_cycle.text = "구매주기 : "+groups[position].first.cycle+" 일"
            holder.tv_price.text = "가격 : "+groups[position].first.price + " 원"
            Glide.with(holder.itemView.context)
                .load(groups[position].first.imageUrl)
                .apply(RequestOptions().circleCrop())
                .into(holder.image)


            holder.btn_cp.setOnClickListener{
                pid = groups[position].second
                cycle = Integer.parseInt(groups[position].first.cycle.toString())
                complete(holder.btn_cp)
            }

            holder.btn_drop.setOnClickListener {
                docRef.get()
                    .addOnSuccessListener { documents ->
                        for(document in documents){
                            var thisId : String?
                            item = document.toObject(ContentDTO::class.java)
                            if(item == groups[position].first){
                                Toast.makeText(this@GroupActivity, "if문 성공", Toast.LENGTH_SHORT).show()
                                thisId = document.id
                                docRef.document(thisId).get().addOnSuccessListener { document ->
                                    item = document.toObject(ContentDTO::class.java)!!
                                    item.Participation[uid.toString()] = false
                                    item.ParticipationCount = item.ParticipationCount - 1
                                    var tsDoc = docRef.document(thisId)
                                    firestore?.runTransaction {
                                            transition->
                                        transition.set(tsDoc, item)
                                    }
                                    //recyclerView?.adapter = RecyclerViewAdapter()
                                    roomsRef.orderByChild("users/$uid").equalTo(true).addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for(room in snapshot.children){
                                                val chatroom = room.getValue<ChatModel>()
                                                if(chatroom?.productid == thisId){
                                                    var roomId = room.key
                                                    docRef.document(thisId).get().addOnSuccessListener { document ->
                                                        var iteminfo = document.toObject(ContentDTO::class.java)
                                                        if(iteminfo?.ParticipationCount == 1){
                                                            roomsRef.child(roomId.toString()).removeValue()
                                                        } else {
                                                            chatroom.users[uid.toString()] = false
                                                            roomsRef.child(roomId.toString()).setValue(chatroom)
                                                        }
                                                    }

                                                }
                                            }
                                        }
                                    })
                                    //recyclerView?.adapter = RecyclerViewAdapter()
                                }
                            }
                        }
                    }
                notifyDataSetChanged()
                recyclerView?.adapter = RecyclerViewAdapter()
            }
        }

        inner class GroupViewHolder(view : View) : RecyclerView.ViewHolder(view){
            val tv_product: TextView = view.findViewById(R.id.productName)
            val image : ImageView = view.findViewById(R.id.list_img)
            val tv_price : TextView = view.findViewById(R.id.price)
            val tv_cycle : TextView = view.findViewById(R.id.cycle)
            val btn_drop : Button = view.findViewById(R.id.button_drop)
            val btn_cp : Button = view.findViewById(R.id.button_complete)
        }

        override fun getItemCount(): Int {
            return groups.size
        }
    }

    fun setAlarm() {
        val calender = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 30)
        }
        Log.d("cycle",cycle.toString())
        calender.add(Calendar.DATE, cycle!!-3)
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        Log.e("날짜", "current: ${df.format(calender.time)}")
        //알람 매니저 가져오기.
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("id",pid)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            M_ALARM_REQUEST_CODE,
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

    companion object {
        private val M_ALARM_REQUEST_CODE = 1000
    }

    fun complete(button:Button){
        val calender = Calendar.getInstance()
        calender.add(Calendar.DATE, cycle!!-3)
        val builder = AlertDialog.Builder(this)
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        builder.setTitle("거래 완료")
            .setMessage("거래 완료 하시겠습니까?\n 다음 거래 날짜는 ${df.format(calender.time)} 입니다.\n다음 거래를 원하지 않을 시 해당 게시물을 삭제해주세요!")
            .setPositiveButton("예",
                DialogInterface.OnClickListener{ dialog, id->
                    setAlarm()
                })
            .setNegativeButton("아니요",
                DialogInterface.OnClickListener{ dialog, id->
                })
        builder.show()

    }

}