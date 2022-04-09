package com.techtown.matchingservice.model

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
    var ParticipationCount : Int = 1,
    var ParticipationTotal :Int = 0,
    var location : GeoPoint = GeoPoint(37.5466,126.9661),
    //좋아요 누른 유저 관리
    var Participation : MutableMap<String, Boolean> = HashMap()){
    //댓글 관리
    data class Comment(
        var uid : String? = null,
        var userId : String? = null,
        var comment : String? = null,
        var timestamp : Long? = null)
}