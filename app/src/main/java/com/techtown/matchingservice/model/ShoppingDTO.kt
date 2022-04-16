package com.techtown.matchingservice.model

data class ShoppingDTO (
    var store : String? = null,
    var check : Boolean = false,
    var order_price : Int = 0,
    var shopping_price : Int = 0,
    var shopping_address : String? = null,
    var shopping_detail : String? = null,
    var shopping_uid : String? = null,
    var shopping_userId : String? = null,
    var shopping_timestamp : Long? = null,
    var shopping_ParticipationCount : Int = 0,
    //참여 누른 유저 관리
    var shoppingParticipation : MutableMap<String, Boolean> = HashMap()){
        //댓글 관리
        data class Comment(
            var uid : String? = null,
            var userId : String? = null,
            var comment : String? = null,
            var timestamp : Long? = null)
}