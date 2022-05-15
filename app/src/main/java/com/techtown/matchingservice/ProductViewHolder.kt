package com.techtown.matchingservice

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.product_item.view.*

class ProductViewHolder (v: View) : RecyclerView.ViewHolder(v) {
    var view : View = v
    fun bind(item:ProductData) {
        //view.productitem_textview_userId.text = item.userId
        view.productitem_textview_product_name.text = item.name
        //view.productitem_participation.text = item.participation
        view.productitem_textview_place.text = item.price + " / " + item.participationCount + "개"
        Glide.with(view.context).load(item.imageUri).into(view.productItem_photo)
        view.text_numofProduct.text = "1인당 " + item.unit.toString() + "개"
        var timeLong : Long? = item.timestamp.toLong()
        view.text_time.text = timeDiff(timeLong!!)
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
                    msg = i.msg
                    break
                }
            }
        }
        return diffTime.toString() + msg
    }
}