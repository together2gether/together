package com.techtown.matchingservice

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.techtown.matchingservice.databinding.Fragment1Binding
import com.techtown.matchingservice.databinding.ProductItemBinding
import com.techtown.matchingservice.model.ContentDTO

class Fragment1 : Fragment() {
    private lateinit var binding: Fragment1Binding
    var firestore: FirebaseFirestore? = null
    lateinit var uid : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = Fragment1Binding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().uid!!


        binding.imageButton2.setOnClickListener {
            val intent = Intent(context, SearchActivity::class.java)
            startActivity(intent)
        }
        binding.button51.setOnClickListener {
            val intent = Intent(context, ConditionActivity::class.java)
            startActivity(intent)
        }
        binding.button52.setOnClickListener {
            val intent = Intent(context, ProductActivity::class.java)
            startActivity(intent)
        }
        binding.button3.setOnClickListener {
            val string = binding.edit.text
            if (string.isNullOrEmpty()) {
                Toast.makeText(context, "chip 이름을 입력해주세요", Toast.LENGTH_LONG).show()
            } else {
                binding.chipGroup.addView(Chip(context).apply {
                    text = string
                    chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))

                    chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#cdd9f1"))
                    chipStrokeWidth = 4f
                    setTextColor(
                        ColorStateList.valueOf(Color.parseColor("#000000"))
                    )
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { binding.chipGroup.removeView(this) }
                })
            }
        }

        binding.fragment1RecyclerView.adapter = Fragment1RecyclerviewAdapter()
        binding.fragment1RecyclerView.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    inner class CustomViewHolder(var binding: ProductItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class Fragment1RecyclerviewAdapter() : RecyclerView.Adapter<CustomViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")
                ?.orderBy("timestamp")
                ?.addSnapshotListener { value, error ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    for (snapshot in value!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()

                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            var view =
                ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            var viewHolder = holder.binding
            //UserId
            viewHolder.productitemTextviewUserId.text = contentDTOs[position].userId
            //ProductName
            viewHolder.productitemTextviewProductName.text = contentDTOs[position].product
            //place
            viewHolder.productitemTextviewPlace.text = contentDTOs[position].place
            //Photo
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
                .into(viewHolder.productItemPhoto)

            var participationCount:String = contentDTOs[position].ParticipationCount.toString()

            viewHolder.productitemParticipation.text = "현재 "+participationCount+" / "+contentDTOs[position].ParticipationTotal.toString()
            //click
            viewHolder.productitemCardView.setOnClickListener{
                Intent(context, Product::class.java).apply {
                    putExtra("product", contentDTOs[position].product)
                    putExtra("imageUrl", contentDTOs[position].imageUrl)
                    putExtra("price", contentDTOs[position].price.toString())
                    putExtra("totalNumber", contentDTOs[position].totalNumber.toString())
                    putExtra("cycle", contentDTOs[position].cycle.toString())
                    putExtra("unit", contentDTOs[position].unit.toString())
                    putExtra("URL", contentDTOs[position].url)
                    putExtra("place", contentDTOs[position].place)
                    putExtra("timestamp", contentDTOs[position].timestamp.toString())
                    putExtra("participationCount", participationCount)
                    putExtra("uidkey",contentDTOs[position].Participation.containsKey(uid).toString())
                    putExtra("participationTotal", contentDTOs[position].ParticipationTotal.toString())
                    putExtra("id", contentUidList[position])
                    putExtra("Uid", contentDTOs[position].uid.toString())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context?.startActivity(this) }
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }
}