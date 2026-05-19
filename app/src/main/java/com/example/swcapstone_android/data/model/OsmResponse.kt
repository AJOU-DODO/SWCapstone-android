package com.example.swcapstone_android.data.model

data class OsmResponse(
    val elements: List<OsmElement>
)

data class OsmElement(
    val type: String,
    val id: Long,
    val geometry: List<OsmGeometry>?
)

data class OsmGeometry(
    val lat: Double,
    val lon: Double
)