package com.techtown.matchingservice

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.food_item.view.*
import kotlinx.android.synthetic.main.product_item.view.*

class DeliveryViewHolder (v: View) : RecyclerView.ViewHolder(v) {
    var view : View = v
    fun bind(item:DeliveryData) {
        view.fooditemTextviewstore.text = item.store
        view.fooditemTextviewdeliveryprice.text = (Integer.parseInt(item.deliverprice)/2).toString()
        view.fooditemTextvieworderprice.text = item.orderprice
        Glide.with(view.foodimage.context).load(item.imageurl)
            .apply(RequestOptions().circleCrop())
            .into(view.foodimage)
    }
}