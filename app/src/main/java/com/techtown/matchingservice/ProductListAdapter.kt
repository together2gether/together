package com.techtown.matchingservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ProductListAdapter(val itemList : List<ProductData>) : RecyclerView.Adapter<ProductViewHolder>() {
    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
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