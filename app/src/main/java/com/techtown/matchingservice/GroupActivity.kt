package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

class GroupActivity : AppCompatActivity() {
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null

    var item = ContentDTO()

    val db = Firebase.firestore
    val docRef = db.collection("images")
    var firestore : FirebaseFirestore? = null

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

        val finish = findViewById<Button>(R.id.button47)
        finish.setOnClickListener {
            finish()
        }
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.GroupViewHolder>(){
        var groups = ArrayList<ContentDTO>()
        init {
            groups.clear()
            docRef.get()
                .addOnSuccessListener { documents ->
                    for(document in documents){
                        item = document.toObject(ContentDTO::class.java)!!
                        if(item?.Participation?.get(uid) == true){
                            //Toast.makeText(this@GroupActivity, item.product.toString(), Toast.LENGTH_SHORT).show()
                            groups.add(item)
                        }
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
            if(groups[position].uid.toString() == uid.toString()){
                holder.btn_drop.visibility = View.INVISIBLE
            }
            holder.tv_product.text = groups[position].product
            holder.tv_cycle.text = "구매주기 : "+groups[position].cycle+" 일"
            holder.tv_price.text = "가격 : "+groups[position].price + " 원"
            Glide.with(holder.itemView.context)
                .load(groups[position].imageUrl)
                .apply(RequestOptions().circleCrop())
                .into(holder.image)

            holder.btn_drop.setOnClickListener {
                val builder = AlertDialog.Builder(this@GroupActivity)
                builder.setTitle("탈퇴")
                    .setMessage(groups[position].product + "공동구매에서 탈퇴 하시겠습니까?")
                    .setPositiveButton("예",
                    DialogInterface.OnClickListener{dialog, id->
                        docRef.get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {
                                    var thisId: String?
                                    item = document.toObject(ContentDTO::class.java)!!
                                    if (item == groups[position]) {
                                        thisId = document.id
                                        var tsDoc =
                                            firestore?.collection("images")?.document(thisId)
                                        firestore?.runTransaction { transition ->
                                            var contentDTO = transition.get(tsDoc!!)
                                                .toObject(ContentDTO::class.java)!!
                                            contentDTO.Participation[uid.toString()] = false
                                            contentDTO.ParticipationCount -= 1
                                            transition.set(tsDoc, contentDTO)
                                        }
                                        roomsRef.orderByChild("productid").equalTo(thisId)
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onCancelled(error: DatabaseError) {
                                                }

                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    for (room in snapshot.children) {
                                                        val chatroom = room.getValue<ChatModel>()
                                                        var roomId = room.key
                                                        if (item?.ParticipationCount == 2) {
                                                            roomsRef.child(roomId.toString())
                                                                .removeValue()
                                                        } else {
                                                            chatroom!!.users[uid.toString()] = false
                                                            roomsRef.child(roomId.toString())
                                                                .setValue(chatroom)
                                                        }
                                                    }
                                                }
                                            })
                                    }
                                }
                            }
                        finish()
                    })
                    .setNegativeButton("아니오",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.show()
                recyclerView?.adapter = RecyclerViewAdapter()
            }
        }

        inner class GroupViewHolder(view : View) : RecyclerView.ViewHolder(view){
            val tv_product: TextView = view.findViewById(R.id.productName)
            val image : ImageView = view.findViewById(R.id.list_img)
            val tv_price : TextView = view.findViewById(R.id.price)
            val tv_cycle : TextView = view.findViewById(R.id.cycle)
            val btn_drop : Button = view.findViewById(R.id.button_drop)
        }

        override fun getItemCount(): Int {
            return groups.size
        }

    }
}