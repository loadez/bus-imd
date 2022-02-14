package dev.loadez.bus.domain

data class LocationModel(
    val id: Int?,
    val vehicle_id:Int,
    val latitude:Float,
    val longitude:Float,
    val timestamp: Long,
)