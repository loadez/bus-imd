package dev.loadez.bus.domain

data class BusModel(
    val id:Int?,
    val vehicle_id:String,
    val plate:String,
    val label:String,
)