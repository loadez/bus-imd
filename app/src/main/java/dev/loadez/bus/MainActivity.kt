package dev.loadez.bus

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider

import org.osmdroid.views.overlay.compass.CompassOverlay




class MainActivity : AppCompatActivity() {

    private lateinit var map:MapView
    private lateinit var mapController:MapController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            if (it[Manifest.permission.ACCESS_FINE_LOCATION]==true || it[Manifest.permission.ACCESS_COARSE_LOCATION]==true){

            }
            else{

            }
        };
        val permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION);
        locationPermissionRequest.launch(permissions.toTypedArray());


        setContentView(R.layout.activity_main)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.controller as MapController
        mapController.setZoom(16.0)
        mapController.setCenter(GeoPoint(-5.8322895,-35.2055337))

    }

    override fun onResume() {
        super.onResume()
        map.onResume();
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}