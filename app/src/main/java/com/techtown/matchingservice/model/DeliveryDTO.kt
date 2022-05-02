package com.techtown.matchingservice.model

data class DeliveryDTO (
    var store : String? = null,
    var name : String? = null,
    var delivery : Boolean = false,
    var order_price : Int = 0,
    var delivery_price : Int = 0,
    var delivery_detail : String? = null,
    var delivery_uid : String? = null,
    var delivery_userId : String? = null,
    var delivery_timestamp : Long? = null,
    var delivery_ParticipationCount : Int = 0,
    //참여 누른 유저 관리
    var deliveryParticipation : MutableMap<String, Boolean> = HashMap()){

}