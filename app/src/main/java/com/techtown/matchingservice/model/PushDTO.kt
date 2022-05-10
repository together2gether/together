package com.techtown.matchingservice.model

class PushDTO (
    var to : String? = null,
    var notification : Notification = Notification()){
    data class Notification(
        var title : String? = null,
        var body : String? = null
    )
}