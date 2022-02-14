package dev.loadez.bus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import dev.loadez.bus.application.LocationDAO
import dev.loadez.bus.domain.LocationModel
import java.util.*

class BusInfo : AppCompatActivity() {
    private var vehicleId:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_info)
        vehicleId = intent.getIntExtra("vehicle_id",-1)
        if(vehicleId==-1){
            finish()
        }


        val locations = LocationDAO.getLocationByVehicleId(vehicleId)
            .map {
            "${Date(it.timestamp*1000)} - Latitude: ${it.latitude} Longitude ${it.longitude}"
        }
        val locationList = findViewById<ListView>(R.id.locationList)
        val arrayAdapter = ArrayAdapter(this,R.layout.location_layout,locations)
        locationList.adapter = arrayAdapter
    }
}