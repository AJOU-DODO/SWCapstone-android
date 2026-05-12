package com.example.swcapstone_android.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(private val context: Context) {
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    // 지오펜스 등록 함수
    @SuppressLint("MissingPermission")
    fun addGeofence(id: String, lat: Double, lng: Double) {
        val geofence = Geofence.Builder()
            .setRequestId(id) // 둥지 ID
            .setCircularRegion(lat, lng, 10f) // 10m 반경
            .setExpirationDuration(Geofence.NEVER_EXPIRE) // 수동 해제 전까지 유지
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER) // 진입 시에만 알림
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        // 시스템이 깨울 통로 (PendingIntent)
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.addGeofences(request, pendingIntent).addOnSuccessListener {
            Log.d("Geofence", "10m 반경 지오펜스 등록 성공!")
        }
    }
}