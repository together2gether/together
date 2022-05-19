package com.techtown.matchingservice.util

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
    var item = ContentDTO()
    private var uid : String? = null

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "1000"
        const val NOTIFICATION_ID = 100
    }

    override fun onReceive(context: Context, intent: Intent) {
        firestore = FirebaseFirestore.getInstance()
        uid = Firebase.auth.currentUser?.uid.toString()
        id = intent.getStringExtra("id").toString()

        docRef.document("$id" ).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    item = document.toObject(ContentDTO::class.java)!!
                    for (users in item!!.Participation) {
                        if (users.value == true) {
                            if (users.key != uid) {
                                var str = item.product.toString()+"의 다음 공동 구매가 3일 남았습니다."
                                FcmPush.instance.sendMessage(users.key, "공동구매 알림",str)
                            }
                        }
                    }
                }
            }

        // 채널 생성
        createNotificationChannel(context)
        // 알림
        notifyNotification(context)

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

    private fun notifyNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("공동구매 알림")
                .setContentText("다음 공동구매가 3일 남았습니다. 참여자들에게 연락을 해주세요.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)

            notify(NOTIFICATION_ID, build.build())
        }
    }
}