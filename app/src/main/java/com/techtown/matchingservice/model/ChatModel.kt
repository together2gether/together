package com.techtown.matchingservice.model

class ChatModel (val users: HashMap<String, Boolean> = HashMap(),
                 val comments : HashMap<String, Comment> = HashMap(),
                 var productid : String? = null,
                 var delivery : Boolean? = false,
){
    class Comment(val uid: String? = null, val message: String? = null, val time: String? = null, val longtime: Long? = null)
}