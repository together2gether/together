package com.techtown.matchingservice

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class LatLngData(/*val index : Int, val id : Long,*/ val latlng : LatLng) : ClusterItem {
    override fun getPosition(): LatLng {
        return latlng
    }

    override fun getTitle(): String? {
        return "a"
    }

    override fun getSnippet(): String? {
        return "b"
    }

}