package dev.loadez.bus.domain

data class BusModel(
    val id:String,
    var latitude:Double,
    var longitude:Double,
    val company:String,
    val route: String,
)