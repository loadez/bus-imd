package dev.loadez.bus.domain

data class RouteModel (
    val id:Int?,
    val routeId:String,
    val agencyId:Int,
    val shortName:String,
    val longName:String,
)