package com.techtown.matchingservice

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class LatLngData(val index : Int,  val id : String?, val latlng : LatLng) :ClusterItem {
    override fun getPosition(): LatLng {
        return latlng
    }

    override fun getSnippet(): String? {
        return ""
    }

    override fun getTitle(): String? {
        return ""
    }

}