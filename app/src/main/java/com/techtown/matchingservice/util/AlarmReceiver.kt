package com.techtown.matchingservice.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.R
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.DeliveryDTO

class AlarmReceiver : BroadcastReceiver() {

    var firestore: FirebaseFirestore? = null
    val db = Firebase.firestore
    val docRef = db.collection("images")
    var id : String? = null
    var productid : String? = null
    var item = ContentDTO()
    var product : String? = null
    var contentDTO = ContentDTO()
    private var uid : String? = null

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "1000"
    }

    override fun onReceive(context: Context, intent: Intent) {
        firestore = FirebaseFirestore.getInstance()
        uid = Firebase.auth.currentUser?.uid.toString()
        id = intent.getStringExtra("id").toString()
        productid = intent.getStringExtra("productid").toString()

        docRef.document("$id" ).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    item = document.toObject(ContentDTO::class.java)!!
                    for (users in item!!.Participation) {
                        if (users.value == true) {
                            if (users.key != uid) {
                                var str = item.product.toString()+"의 다음 공동 구매가 3일 남았습니다."
                                if(product != item.product.toString() ){
                                        product = item.product.toString()
                                        FcmPush.instance.sendMessage(users.key, "공동구매 알림", str)
                                        createNotificationChannel(context)
                                        notifyNotification(context, product.toString())
                                    }
                            }
                        }
                    }
                }
            }


    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "공동구매 알림",
                NotificationManager.IMPORTANCE_HIGH
            )

            NotificationManagerCompat.from(context)
                .createNotificationChannel(notificationChannel)
        }
        Log.e("알림", "성공")
    }

    @SuppressLint("ResourceAsColor")
    private fun notifyNotification(context: Context, product : String) {
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("공동구매 알림")
                .setContentText(product+"다음 공동구매가 3일 남았습니다. 참여자들에게 연락을 해주세요.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.icon_message)
                .setColor(R.color.icon_color)

            notify((System.currentTimeMillis()).toInt(), build.build())
        }
    }
}