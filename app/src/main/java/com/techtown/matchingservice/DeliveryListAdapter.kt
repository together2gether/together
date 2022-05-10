package com.techtown.matchingservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DeliveryListAdapter(val itemList : List<DeliveryData>) : RecyclerView.Adapter<DeliveryViewHolder>() {
    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.food_item, parent, false)
        return DeliveryViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
        holder.apply {
            bind(item)
        }
    }
    interface OnItemClickListener {
        fun onClick(v: View, position:Int)
    }
    private lateinit var itemClickListener : OnItemClickListener
    fun setItemClickListener(itemClickListener : OnItemClickListener){
        this.itemClickListener = itemClickListener
    }
}