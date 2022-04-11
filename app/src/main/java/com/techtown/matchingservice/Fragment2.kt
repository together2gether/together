package com.techtown.matchingservice


import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.techtown.matchingservice.databinding.FoodItemBinding
import com.techtown.matchingservice.databinding.Fragment2Binding
import com.techtown.matchingservice.model.DeliveryDTO

class Fragment2 : Fragment() {
    private lateinit var binding: Fragment2Binding
    var firestore: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = Fragment2Binding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        binding.imageButton2.setOnClickListener {
            val intent = Intent(context, SearchActivity::class.java)
            startActivity(intent)
        }
        binding.button3.setOnClickListener {
            val string = binding.edit.text
            if (string.isNullOrEmpty()) {
                Toast.makeText(context, "chip 이름을 입력해주세요", Toast.LENGTH_LONG).show()
            } else {
                binding.chipGroup2.addView(Chip(context).apply {
                    text = string
                    chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))

                    chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#cdd9f1"))
                    chipStrokeWidth = 4f
                    setTextColor(
                        ColorStateList.valueOf(Color.parseColor("#000000"))
                    )
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { binding.chipGroup2.removeView(this) }
                })
            }
        }
        binding.fragment2RecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        binding.fragment2RecyclerView.adapter =Fragment2RecyclerviewAdapter()
        binding.fragment2RecyclerView.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    inner class DeliveryViewHolder(var binding: FoodItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class Fragment2RecyclerviewAdapter() : RecyclerView.Adapter<DeliveryViewHolder>() {

        var deliveryDTOs: ArrayList<DeliveryDTO> = arrayListOf()
        var deliveryUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("delivery")
                ?.orderBy("delivery_timestamp")
                ?.addSnapshotListener { value, error ->
                    deliveryDTOs.clear()
                    deliveryUidList.clear()
                    for (snapshot in value!!.documents) {
                        var item = snapshot.toObject(DeliveryDTO::class.java)
                        deliveryDTOs.add(item!!)
                        deliveryUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): DeliveryViewHolder {
            var view =
                FoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DeliveryViewHolder(view)
        }

        override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
            var viewHolder = holder.binding
            //store
            viewHolder.fooditemTextviewstore.text = deliveryDTOs[position].store
            //order price
            viewHolder.fooditemTextvieworderprice.text = deliveryDTOs[position].order_price.toString()
            //delivery price
            viewHolder.fooditemTextviewdeliveryprice.text = deliveryDTOs[position].delivery_price.toString()
            //click
            viewHolder.fooditemCardView.setOnClickListener{
                Intent(context, Delivery::class.java).apply{
                    putExtra("store",deliveryDTOs[position].store)
                    putExtra("orderPrice",deliveryDTOs[position].order_price.toString())
                    putExtra("deliveryPrice",deliveryDTOs[position].delivery_price.toString())
                    putExtra("deliveryAddress",deliveryDTOs[position].delivery_address,)
                    putExtra("deliveryid", deliveryUidList[position])
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run{context?.startActivity(this)}
            }
        }

        override fun getItemCount(): Int {
            return deliveryDTOs.size
        }
    }
}