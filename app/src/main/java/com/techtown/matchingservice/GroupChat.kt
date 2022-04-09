package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.model.ChatModel
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.UsersInfo
import java.text.SimpleDateFormat
import java.util.*

class GroupChat : AppCompatActivity(){
    private var chatRoomUid : String? = null
    //private var destinationUid : ArrayList<String>? = null
    private var productid : String? = null
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null
    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val roomsRef = database.getReference("chatrooms")
    private val usersRef = database.getReference("usersInfo")

    var firestore: FirebaseFirestore? = null

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        Toast.makeText(this, "확인", Toast.LENGTH_LONG).show()
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.groupchat)

        val imageView = findViewById<Button>(R.id.btn_input)
        val editText = findViewById<EditText>(R.id.editText_msg)

        //메시지를 보낸 시간
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        productid = intent.getStringExtra("productid").toString()
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = findViewById(R.id.msg_recyclerview)

        firestore = FirebaseFirestore.getInstance()

        imageView.setOnClickListener {
            val chatModel = ChatModel()
            chatModel.productid = productid.toString()
            chatModel.users.put(uid.toString(), true)
            var tsDoc = firestore?.collection("images")?.document(productid.toString())
            firestore?.runTransaction {
                transition ->
                var item = transition.get(tsDoc!!).toObject(ContentDTO::class.java)
                for(users in item!!.Participation){
                    chatModel.users.put(uid.toString(), true)
                }
            }
            /*firestore?.collection("images")
                ?.document(productid.toString())
                ?.addSnapshotListener{ value, error ->
                    var item = value!!.toObject(ContentDTO::class.java)

                }*/
            val comment = ChatModel.Comment(uid, editText.text.toString(), curTime)
            if(chatRoomUid == null){
                imageView.isEnabled = false
                roomsRef.push().setValue(chatModel).addOnSuccessListener {
                    //채팅방 생성
                    checkChatRoom()
                    Handler().postDelayed({
                        roomsRef.child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                        editText.text = null
                    }, 1000L)
                }
            } else {
                roomsRef.child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                editText.text = null
            }
        }
        checkChatRoom()
    }
    private fun checkChatRoom(){
        roomsRef.orderByChild("users/$uid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children){
                        val chatModel = item.getValue<ChatModel>()
                        var alluser = true

                        var tsDoc = firestore?.collection("images")?.document(productid.toString())
                        firestore?.runTransaction {
                                transition ->
                            var item = transition.get(tsDoc!!).toObject(ContentDTO::class.java)
                            for(users in item!!.Participation){
                                if(!chatModel?.users!!.containsKey(users.toString())){
                                    alluser = false
                                }
                            }
                        }

                        /*firestore?.collection("images")
                            ?.document(productid.toString())
                            ?.addSnapshotListener{ value, error ->
                                var item = value!!.toObject(ContentDTO::class.java)
                                for(users in item!!.Participation){
                                    if(!chatModel?.users!!.containsKey(users.toString())){
                                        alluser = false
                                    }
                                }
                            }*/
                        if(alluser){
                            chatRoomUid = item.key
                            val button = findViewById<Button>(R.id.btn_input)
                            button.isEnabled = true
                            recyclerView?.layoutManager = LinearLayoutManager(this@GroupChat)
                            recyclerView?.adapter = RecyclerViewAdapter()
                        }
                    }
                }
            })
    }
    inner  class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>(){
        private val comments = ArrayList<ChatModel.Comment>()
        //private var user : UserInfo? = null
        init {
            var tsDoc = firestore?.collection("images")?.document(productid.toString())
            firestore?.runTransaction {
                    transition ->
                var item = transition.get(tsDoc!!).toObject(ContentDTO::class.java)
                var topName = findViewById<TextView>(R.id.textView_topName)
                topName.text = item?.product.toString()
                getMessageList()
            }

            /*firestore?.collection("images")
                ?.document(productid.toString())
                ?.addSnapshotListener{ value, error ->
                    var item = value!!.toObject(ContentDTO::class.java)
                    var topName = findViewById<TextView>(R.id.textView_topName)
                    topName.text = item?.product.toString()
                    getMessageList()
                }*/
        }
        fun getMessageList(){
            roomsRef.child(chatRoomUid.toString()).child("comments").addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for(data in snapshot.children){
                        val item = data.getValue<ChatModel.Comment>()
                        comments.add(item!!)
                    }
                    notifyDataSetChanged()
                    recyclerView?.scrollToPosition(comments.size -1)
                }
            })
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerViewAdapter.MessageViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.message_left_item, parent, false)
            return MessageViewHolder(view)
        }

        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(
            holder: RecyclerViewAdapter.MessageViewHolder,
            position: Int
        ) {
            holder.textView_message.textSize = 20F
            holder.textView_message.text = comments[position].message
            holder.textView_time.text = comments[position].time
            if(comments[position].uid.equals(uid)){
                holder.textView_message.setBackgroundResource(R.drawable.right_item_message)
                holder.layout_destination.visibility = View.INVISIBLE
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
                holder.textView_time.gravity = Gravity.RIGHT
            } else {
                var user : UsersInfo? = null
                usersRef.child(comments[position].uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        user = snapshot.getValue<UsersInfo>()
                        Glide.with(holder.itemView.context)
                            .load(user?.profileImageUrl)
                            .apply(RequestOptions().circleCrop())
                            .into(holder.imageView_profile)
                        holder.textView_name.text = user?.name
                        holder.layout_destination.visibility= View.VISIBLE
                        holder.textView_name.visibility = View.VISIBLE
                        holder.textView_message.setBackgroundResource(R.drawable.left_item_message)
                        holder.layout_main.gravity = Gravity.LEFT
                    }

                })


            }
        }
        inner class MessageViewHolder(view : View) : RecyclerView.ViewHolder(view){
            val textView_message: TextView = view.findViewById(R.id.tv_comment)
            val textView_name: TextView = view.findViewById(R.id.tv_name)
            val imageView_profile: ImageView = view.findViewById(R.id.iv_profile)
            val layout_destination: LinearLayout = view.findViewById(R.id.linear_destination)
            val layout_main: LinearLayout = view.findViewById(R.id.linear_main)
            val textView_time: TextView = view.findViewById(R.id.tv_time)
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }


}