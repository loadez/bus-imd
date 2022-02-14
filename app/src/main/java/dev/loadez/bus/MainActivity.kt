package dev.loadez.bus

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.loadez.bus.domain.BusModel
import dev.loadez.bus.domain.LocationModel
import dev.loadez.bus.infraestructure.DbAdapter
import dev.loadez.bus.service.BusService
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme


class MainActivity : AppCompatActivity() {

    private lateinit var map:MapView
    private lateinit var mapController:MapController
    private lateinit var simplePointTheme: SimplePointTheme
    private lateinit var simpleFastPointOverlayOptions: SimpleFastPointOverlayOptions
    private lateinit var simpleFastPointOverlay: SimpleFastPointOverlay

    private val locations = mutableListOf<LocationModel>()
    private val points = mutableListOf<IGeoPoint>()

    private val broadCastReceiver:BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.hasExtra("Locations")) {
                val updates:Array<LocationModel> = intent.extras!!["Locations"] as Array<LocationModel>

                for(i in updates){
                    val index = locations.indexOfFirst { it.vehicle_id==i.vehicle_id }
                    if(index!=-1){
                        locations.removeAt(index)
                    }
                    locations.add(i)

                }

                points.clear()
                for(i in locations){
                    points.add(LabelledGeoPoint(i.latitude.toDouble(),i.longitude.toDouble(),i.vehicle_id.toString()))
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DbAdapter.initializeDatabase(this)
        runService()
        setContentView(R.layout.activity_main)
        initializeMap()
        createFastOverlay()
        checkPermissions()
    }

    private fun runService(){
        val intent = Intent(this,BusService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(intent)
        }
        else{
            applicationContext.startService(intent)
        }
    }


    private fun createFastOverlay(){
        simplePointTheme = SimplePointTheme(points)

        simpleFastPointOverlayOptions = SimpleFastPointOverlayOptions()

        simpleFastPointOverlay = SimpleFastPointOverlay(simplePointTheme,simpleFastPointOverlayOptions)

        simpleFastPointOverlay.setOnClickListener { points, point ->  handlePointClick(points,point)}

        map.overlays.add(simpleFastPointOverlay)
    }


    private fun handlePointClick(points: SimpleFastPointOverlay.PointAdapter, point: Int) {
        val target = points.get(point) as LabelledGeoPoint
        val location = locations.firstOrNull{target.label==it.vehicle_id.toString()}
        if (location!=null){
            Toast.makeText(applicationContext,"Você clicou no ônibus ${location.vehicle_id}",Toast.LENGTH_SHORT).show()
            val intent:Intent = Intent(this,BusInfo::class.java)
            intent.putExtra("vehicle_id",location.vehicle_id)
            startActivity(intent)
        }
    }

    private  fun checkPermissions(){
        //Requisita as permissões
        val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            if (it[Manifest.permission.ACCESS_FINE_LOCATION]==true || it[Manifest.permission.ACCESS_COARSE_LOCATION]==true){
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map);
                locationOverlay.enableMyLocation()
                map.overlays.add(locationOverlay)
            }
            else{

            }
        };
        val permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION);
        locationPermissionRequest.launch(permissions.toTypedArray());

    }

    private fun initializeMap(){

        //Configura o user agent para as requisições de tiles
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        //Configura o mapa pela primeira vez
        map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.controller as MapController
        mapController.setZoom(16.0)
        mapController.setCenter(GeoPoint(-5.8322895,-35.2055337))

        val marker = Marker(map)
        marker.position = GeoPoint(-5.8322895,-35.2055337)
        map.overlays.add(marker)




    }

    override fun onResume() {
        super.onResume()
        map.onResume();

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadCastReceiver,IntentFilter("BusesUpdate"))
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadCastReceiver)
    }
}