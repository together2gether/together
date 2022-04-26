package com.techtown.matchingservice.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

data class ContentDTO(
    var product : String? = null,
    var imageUrl : String? =null,
    var price : Int = 0,
    var totalNumber : Int = 0,
    var unit : Int = 0,
    var cycle : Int = 0 ,
    var url : String? = null,
    var place : String? = null,
    var uid : String? = null,
    var userId : String? = null,
    var timestamp : Long? = null,
    var ParticipationCount : Int = 0,
    var ParticipationTotal :Int = 0,
    var location : GeoPoint = GeoPoint(37.5466,126.9661),
    //참여 누른 유저 관리
    var Participation : MutableMap<String, Boolean> = HashMap()){
}