package com.techtown.matchingservice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.model.ContentDTO

class GroupActivity : AppCompatActivity() {
    private var productid : String? = null
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null

    var item = ContentDTO()

    private val groups = ArrayList<ContentDTO>()

    val db = Firebase.firestore
    val docRef = db.collection("images")

    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group)

        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = findViewById(R.id.group_recycler)

        firestore = FirebaseFirestore.getInstance()

        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = RecyclerViewAdapter()

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
                        if(item?.Participation!!.containsKey(uid)){
                            Toast.makeText(this@GroupActivity, item.product.toString(), Toast.LENGTH_SHORT).show()
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

        override fun onBindViewHolder(holder: RecyclerViewAdapter.GroupViewHolder, position: Int) {
            holder.tv_product.text = groups[position].product
            holder.tv_cycle.text = "구매주기 : "+groups[position].cycle+" 일"
            holder.tv_price.text = "가격 : "+groups[position].price + " 원"
            holder.part_Count.text = "현재 인원 : "+groups[position].ParticipationCount+"/"+groups[position].ParticipationTotal
            Glide.with(holder.itemView.context)
                .load(groups[position].imageUrl)
                .apply(RequestOptions().circleCrop())
                .into(holder.image)
        }

        inner class GroupViewHolder(view : View) : RecyclerView.ViewHolder(view){
            val tv_product: TextView = view.findViewById(R.id.productName)
            val image : ImageView = view.findViewById(R.id.moodImageView)
            val tv_price : TextView = view.findViewById(R.id.price)
            val tv_cycle : TextView = view.findViewById(R.id.cycle)
            val part_Count : TextView = view.findViewById(R.id.participation_Count)
        }

        override fun getItemCount(): Int {
            return groups.size
        }

    }
}