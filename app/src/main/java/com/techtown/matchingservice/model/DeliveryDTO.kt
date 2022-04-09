package com.techtown.matchingservice.model

data class DeliveryDTO (
    var store : String? = null,
    var order_price : Int = 0,
    var delivery_price : Int = 0,
    var delivery_address : String? = null,
    var delivery_uid : String? = null,
    var delivery_userId : String? = null,
    var delivery_timestamp : Long? = null,
    //참여 누른 유저 관리
    var Participation : MutableMap<String, Boolean> = HashMap()){
        //댓글 관리
        data class Comment(
            var uid : String? = null,
            var userId : String? = null,
            var comment : String? = null,
            var timestamp : Long? = null)
}