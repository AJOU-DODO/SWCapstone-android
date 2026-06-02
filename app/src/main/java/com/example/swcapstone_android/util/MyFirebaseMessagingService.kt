package com.example.swcapstone_android.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.swcapstone_android.ui.MainActivity
import com.example.swcapstone_android.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        // 1. 백엔드 로그에 찍힌 키값(SELECTED_NEST_ID, NOTIFICATION_TYPE)으로 수정
        Log.d("FCM_SERVICE", "FCM 폰에 도착 완료! 원본 데이터 맵: $data")

        // 🌟 [핵심 보정] 대문자 규격과 소문자 규격 둘 중 하나라도 걸리도록 양방향 방어 전략
        val type = data["NOTIFICATION_TYPE"] ?: data["type"] ?: "UNKNOWN_TYPE"
        val nestId = data["SELECTED_NEST_ID"] ?: data["nestId"] ?: "0"

        // 백엔드에서 body에 데이터를 다 넣었으므로 data["title"]과 data["body"]를 우선 확인
        val title = data["title"] ?: remoteMessage.notification?.title ?: "DODO"
        val body = data["body"] ?: remoteMessage.notification?.body ?: "새로운 소식이 도착했습니다."

        Log.d("FCM_SERVICE", "알림 수신 성공 -> type: $type, nestId: $nestId, title: $title")

        sendNotification(title, body, type, nestId)
    }

    private fun sendNotification(title: String?, body: String?, type: String?, nestId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("NOTIFICATION_TYPE", type)
            putExtra("SELECTED_NEST_ID", nestId)
        }

        val requestCode = nestId?.hashCode() ?: System.currentTimeMillis().toInt()

        // 2. FLAG_IMMUTABLE을 FLAG_MUTABLE로 변경 (데이터 전달 보장)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, flags)

        val channelId = "dodo_journey_channel_v3"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 상단 팝업을 위한 중요도 설정
            .setDefaults(NotificationCompat.DEFAULT_ALL)   // 소리, 진동 추가
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Dodo Journey", NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(requestCode, notificationBuilder.build())
        Log.d("FCM_SERVICE", "notificationManager.notify() 호출 완료! 상단바 렌더링 명령 전달됨.")
    }
}