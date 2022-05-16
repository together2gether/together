package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.model.ChatModel
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.DeliveryDTO
import com.techtown.matchingservice.model.UsersInfo
import java.util.*


class ChatFragment : Fragment() {
    val database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val roomsRef = database.getReference("chatrooms")
    val usersRef = database.getReference("usersInfo")

    val db = Firebase.firestore
    val docRef = db.collection("images")
    val delRef = db.collection("delivery")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_chat, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.chat_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecyclerViewAdapter()

        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        return view
    }
    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>(){
        private val chatModel = ArrayList<Pair<ChatModel, Long>>()
        private var uid : String? = null
        //private val destinationUsers : ArrayList<String> = arrayListOf()

        init {
            uid = Firebase.auth.currentUser?.uid.toString()
            roomsRef.orderByChild("users/$uid").equalTo(true).addListenerForSingleValueEvent(object :
                ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    chatModel.clear()
                    for(data in snapshot.children){
                        var item = data.getValue<ChatModel>()
                        val commentMap = TreeMap<String, ChatModel.Comment>(reverseOrder())
                        commentMap.putAll(item!!.comments)
                        var lastMessageKey = commentMap.keys.toTypedArray()[0]
                        chatModel.add(Pair(item!!, item.comments[lastMessageKey]?.longtime) as Pair<ChatModel, Long>)
                    }
                    chatModel.sortBy { it.second }
                    chatModel.reverse()
                    notifyDataSetChanged()
                }
            })
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CustomViewHolder {
            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false))
        }
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val imageView: ImageView = itemView.findViewById(R.id.chat_img)
            val textView_title: TextView = itemView.findViewById(R.id.chat_title)
            val textView_lastMessage: TextView = itemView.findViewById(R.id.chat_lastMsg)
            val textView_time : TextView = itemView.findViewById(R.id.textView17)
            val card : CardView = itemView.findViewById(R.id.item_card)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            var p_id = chatModel[position].first.productid.toString()
            var destinationUid: String? = null
            var delivery = chatModel[position].first.delivery
            //채팅바에 있는 유저 모두 체크
            if(p_id == ""){
                for(user in chatModel[position].first.users.keys){
                    if(!user.equals(uid)){
                        destinationUid = user
                    }
                }
                usersRef.child("$destinationUid").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }

                    @SuppressLint("ResourceAsColor")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue<UsersInfo>()
                        Glide.with(holder.itemView.context).load(user?.profileImageUrl)
                            .apply(RequestOptions().circleCrop())
                            .into(holder.imageView)
                        holder.textView_title.text = user?.nickname
                        holder.card.setBackgroundResource(R.color.skyBlue)
                    }
                })
            } else if (delivery == false){
                docRef.document(p_id).get()
                    .addOnSuccessListener { document ->
                        if(document != null){
                            var item = document.toObject(ContentDTO::class.java)
                            holder.textView_title.text = item?.product
                            Glide.with(holder.itemView.context).load(item?.imageUrl)
                                .apply(RequestOptions().circleCrop())
                                .into(holder.imageView)
                        }
                    }
            } else if(delivery == true){
                delRef.document(p_id).get()
                    .addOnSuccessListener { document ->
                        if(document != null){
                            var item = document.toObject(DeliveryDTO::class.java)
                            //holder.textView_title.text = item?.name
                            holder.textView_title.text = item?.store
                            Glide.with(holder.itemView.context).load(item?.imageURL)
                                .apply(RequestOptions().circleCrop())
                                .into(holder.imageView)
                        }
                    }
            }

            //메시지 내림차순 정렬 후 마지막 메세지의 키값을 가져옴
            val commentMap = TreeMap<String, ChatModel.Comment>(reverseOrder())
            commentMap.putAll(chatModel[position].first.comments)
            val lastMessageKey = commentMap.keys.toTypedArray()[0]
            holder.textView_lastMessage.text = chatModel[position].first.comments[lastMessageKey]?.message
            holder.textView_time.text = timeDiff(chatModel[position].second!!)

            //채팅창 선택 시 이동
            holder.itemView.setOnClickListener{
                val intent = Intent(context, chatting::class.java)
                if(p_id == ""){
                    intent.putExtra("destinationUid", destinationUid.toString())
                    intent.putExtra("groupchat", "N")
                } else if(delivery == false) {
                    intent.putExtra("groupchat", "Y")
                    intent.putExtra("productid", p_id)
                } else if ( delivery == true){
                    intent.putExtra("groupchat", "DY")
                    intent.putExtra("productid", p_id)
                }
                context?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return chatModel.size
        }
    }
    enum class TimeValue(val value: Int, val maximum : Int, val msg : String){
        SEC(60,60,"분 전"),
        MIN(60,24,"시간 전"),
        HOUR(24,30,"일 전"),
        DAY(30,12,"달 전"),
        MONTH(12,Int.MAX_VALUE,"년 전")
    }

    fun timeDiff(time : Long): String? {
        val curTime = System.currentTimeMillis()
        var diffTime = (curTime- time)/1000
        var msg:String? = null
        if(diffTime < TimeValue.SEC.value)
            msg = "방금 전"
        else {
            for(i in TimeValue.values()){
                diffTime /= i.value
                if(diffTime < i.maximum){
                    msg = diffTime.toString() + i.msg
                    break
                }
            }
        }
        return msg
    }
}