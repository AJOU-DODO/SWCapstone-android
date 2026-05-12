package com.example.swcapstone_android.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.swcapstone_android.R
import com.example.swcapstone_android.ui.MainActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return

        if (event.hasError()) return

        // 진입(ENTER) 이벤트인지 확인
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = event.triggeringGeofences
            val nestId = triggeringGeofences?.firstOrNull()?.requestId ?: ""
            showNotification(context, nestId)
        }
    }

    private fun showNotification(context: Context, nestId: String) {
        val channelId = "DODO_ARRIVAL"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 안드로이드 8.0 이상 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "도착 알림", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SELECTED_NEST_ID", nestId) // 둥지 ID 전달
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            nestId.hashCode(), // 고유 ID
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("둥지 도착!")
            .setContentText("목적지 주변에 도착했습니다. 탐험을 시작하세요!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}