package com.techtown.matchingservice

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.product_item.view.*

class ProductViewHolder (v: View) : RecyclerView.ViewHolder(v) {
    var view : View = v
    fun bind(item:ProductData) {
        view.productitem_textview_userId.text = item.userId
        view.productitem_textview_product_name.text = item.name
        view.productitem_participation.text = item.participation
        view.productitem_textview_place.text = item.place
        Glide.with(view.context).load(item.imageUri).into(view.productItem_photo)
    }
}