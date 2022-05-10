package com.techtown.matchingservice.util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.techtown.matchingservice.model.PushDTO
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class FcmPush {
    var JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = "key=AAAAVq_QR3E:APA91bFOO6UwFwTJXxvvPA-mKKUe4FRFSQLJ3Lf0FHOJb3xMdU4Ce6vM3RU3AB_pv-KIKo2U5Ofy_PFWVrXBCzm6mkzxwwyl2A5bJMwQ71r4xt3Kt28JLQqSQyHy0J1XcwAPMUU9pYeE"

    var okHttpClient : OkHttpClient? = null
    var gson : Gson? = null
    lateinit var firestore : FirebaseFirestore
    companion object{
        var instance = FcmPush()

    }

    init {
        firestore = FirebaseFirestore.getInstance()
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(dUid : String, title : String, message : String){

        firestore.collection("pushtokens").document(dUid).get().addOnCompleteListener {
                result ->

            var token = result.result["token"].toString()

            var pushModel = PushDTO()
            pushModel.to = token
            pushModel.notification.title = title
            pushModel.notification.body = message

            var body = gson?.toJson(pushModel)!!.toRequestBody(JSON)


            var request = Request.Builder()
                .addHeader("Content-Type","application/json")
                .addHeader("Authorization",serverKey)
                .url(url)
                .post(body)
                .build()


            okHttpClient?.newCall(request)?.enqueue(object : Callback {

                override fun onFailure(call: Call, e: okio.IOException) {
                    TODO("Not yet implemented")
                }

                override fun onResponse(call: Call, response: Response) {
                    print(response.body?.string())
                }
            })
        }


    }
}