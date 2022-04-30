package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        recyclerView?.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        /*groups.clear()
        docRef.get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    item = document.toObject(ContentDTO::class.java)!!
                    if(item?.Participation!!.containsKey(uid)){
                        //Toast.makeText(this, item.product.toString(), Toast.LENGTH_SHORT).show()
                        groups.add(item)
                    }
                }
            }*/
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
                holder.btn_drop.isEnabled = false
            }
            holder.tv_product.text = groups[position].product
            holder.tv_cycle.text = "구매주기 : "+groups[position].cycle+" 일"
            holder.tv_price.text = "가격 : "+groups[position].price + " 원"
            Glide.with(holder.itemView.context)
                .load(groups[position].imageUrl)
                .apply(RequestOptions().circleCrop())
                .into(holder.image)

            holder.btn_drop.setOnClickListener {
                docRef.get()
                    .addOnSuccessListener { documents ->
                        for(document in documents){
                            var thisId : String?
                            item = document.toObject(ContentDTO::class.java)!!
                            if(item == groups[position]){
                                Toast.makeText(this@GroupActivity, "if문 성공", Toast.LENGTH_SHORT).show()
                                thisId = document.id
                                docRef.document(thisId).get().addOnSuccessListener { document ->
                                    item = document.toObject(ContentDTO::class.java)!!
                                    item.Participation[uid.toString()] = false
                                    item.ParticipationCount = item.ParticipationCount - 1
                                    var tsDoc = docRef.document(thisId)
                                    firestore?.runTransaction {
                                            transition->
                                        transition.set(tsDoc!!, item)
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
            val image : ImageView = view.findViewById(R.id.moodImageView)
            val tv_price : TextView = view.findViewById(R.id.price)
            val tv_cycle : TextView = view.findViewById(R.id.cycle)
            val btn_drop : Button = view.findViewById(R.id.button_drop)
        }

        override fun getItemCount(): Int {
            return groups.size
        }

    }
}