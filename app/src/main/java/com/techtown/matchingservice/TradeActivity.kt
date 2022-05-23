package com.techtown.matchingservice

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TradeActivity : AppCompatActivity() {
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null
    var pid : String? = null
    var item_p = ContentDTO()
    var item_d = DeliveryDTO()
    val db = Firebase.firestore
    val docRef = db.collection("images")
    val delRef = db.collection("delivery")
    var firestore : FirebaseFirestore? = null

    var items = ArrayList<Triple<Int, Triple<String, Long, Int>, String>>()

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
                        items.add(Triple(1,Triple(item_p.product, item_p.timestamp, item_p.price / item_p.ParticipationTotal),item_p.imageUrl.toString()) as Triple<Int, Triple<String, Long, Int>, String>)
                    }
                }
                delRef.get()
                    .addOnSuccessListener { documents ->
                        for(document in documents){
                            item_d = document.toObject(DeliveryDTO::class.java)
                            if(item_d.delivery_uid == uid){
                                if(item_d.delivery == true){
                                    items.add(Triple(2,Triple(item_d.store, item_d.delivery_timestamp, null),item_d.imageURL.toString()) as Triple<Int, Triple<String, Long, Int>, String>)
                                } else if(item_d.delivery == false){
                                    items.add(Triple(3,Triple(item_d.store, item_d.delivery_timestamp, null),item_d.imageURL.toString()) as Triple<Int, Triple<String, Long, Int>, String>)
                                }
                            }
                        }
                        items.sortBy { it.second.second }
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
            Glide.with(holder.itemView.context)
                .load(items[position].third)
                .apply(RequestOptions().circleCrop())
                .into(holder.img)
            holder.title.text = items[position].second.first
            val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
            holder.date.text = dateFormat.format(Date(items[position].second.second))
            if(items[position].first != 1) {
                holder.price.text = ""
                holder.textv.visibility = View.INVISIBLE
            } else {
                holder.price.text = items[position].second.third.toString() + " 원"
            }
            holder.card.setOnClickListener {
                if (items[position].first == 1){

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
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }
}