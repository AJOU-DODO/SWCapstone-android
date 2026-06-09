package com.example.swcapstone_android.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class PinResponse(
    val status: String,
    val code: String,
    val message: String,
    val data: List<PinData>
)

data class PinData(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val isAd: Boolean = false
) : ClusterItem { // 👈 ClusterItem 인터페이스 추가

    override fun getPosition(): LatLng = LatLng(latitude, longitude)
    override fun getTitle(): String? = null
    override fun getSnippet(): String? = null
    override fun getZIndex(): Float? = null
}