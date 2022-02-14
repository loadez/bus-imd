package dev.loadez.bus.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.loadez.bus.application.AgencyDAO
import dev.loadez.bus.application.BusDAO
import dev.loadez.bus.application.LocationDAO
import dev.loadez.bus.application.RouteDAO
import dev.loadez.bus.domain.*
import dev.loadez.bus.infraestructure.DbAdapter
import java.lang.Exception
import java.net.URL

class BusService : Service() {
    private  lateinit var workThread: Thread
    private var shouldRun = true

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        DbAdapter.initializeDatabase(this)

        val notification = Notification()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel("BusUpdate","BusUpdate",NotificationManager.IMPORTANCE_LOW))
        }
        val builder = NotificationCompat.Builder(this,"BusUpdate")


        startForeground(1,builder.build())


        //Cria o thread de trabalho
        workThread = Thread{
            var lastStaticUpdate:Long = -1800000
            while (shouldRun){

                try {
                    Log.d("TAG", "onCreate: RODEI!")

                    val now = System.currentTimeMillis()
                    if(now - lastStaticUpdate > 1800000){
                        lastStaticUpdate = now
                        updateStaticInfo()
                    }

                    val url =
                        URL("https://s3-sa-east-1.amazonaws.com/dados.natal.br/FeedMessage.pb")
                    val bytes = url.readBytes()


                    val feed = GtfsRealtime.FeedMessage.parseFrom(bytes)

                    //Adiciona qualquer novo ônibus no banco de dados
                    val buses = mutableListOf<BusModel>()
                    for (e in feed.entityList){
                        buses.add(BusModel(null,e.vehicle.vehicle.id,e.vehicle.vehicle.licensePlate,e.vehicle.vehicle.label))
                    }
                    val busesResult = BusDAO.insertAll(buses)

                    //Adicionar as localizações
                    val locations = mutableListOf<LocationModel>()
                    for (e in feed.entityList){
                        val vehicleId = busesResult.find {
                            it.label == e.vehicle.vehicle.label &&
                                    it.plate == e.vehicle.vehicle.licensePlate &&
                                    it.vehicle_id == e.vehicle.vehicle.id
                        }!!.id
                        locations.add(LocationModel(null,vehicleId!!,e.vehicle.position.latitude,e.vehicle.position.longitude,e.vehicle.timestamp))
                    }
                    val locationsResult = LocationDAO.insertAll(locations)

                    sendBusesUpdate(locationsResult.toTypedArray())
                    Thread.sleep(10000)
                }
                catch (ex:Exception){
                    Log.e("TAG", ex.toString())
                }
            }
        }

        //Algumas configurações do thread
        workThread.isDaemon = true
        workThread.priority = 4
        workThread.start()
    }

    private fun updateStaticInfo(){
        val agencyUrl = URL("https://s3-sa-east-1.amazonaws.com/dados.natal.br/agency.txt")
        val agencyText = agencyUrl.readText()
        val agencyList = mutableListOf<AgencyModel>()
        var skipFirst = true
        for(l in agencyText.split('\n')){
            if(skipFirst){
                skipFirst=false
                continue
            }
            val parts = l.split(',')
            if(parts.size<2){
                continue
            }
            agencyList.add(
                AgencyModel(
                    null,
                    parts[0],
                    parts[1],
                )
            )
        }
        AgencyDAO.insertAll(agencyList)

        val agencies = AgencyDAO.getALl()


        val routeUrl = URL("https://s3-sa-east-1.amazonaws.com/dados.natal.br/routes.txt")
        val routeText = routeUrl.readText()
        val routeList = mutableListOf<RouteModel>()
        skipFirst=true
        for(l in routeText.split('\n')){
            if(skipFirst){
                skipFirst=false
                continue
            }
            val parts = l.split(',')
            if(parts.size<4){
                continue
            }
            routeList.add(
                RouteModel(
                    null,
                    parts[0],
                    agencies.find { it.agencyId==parts[1] }!!.id!!,
                    parts[2],
                    parts[3],
                )
            )
        }
        RouteDAO.insertAll(routeList)
    }

    private fun sendBusesUpdate(locations : Array<LocationModel>){
        //Cria o intent para ser passado
        val intent = Intent("BusesUpdate")

        //Adiciona ela como um extra
        intent.putExtra("Locations",locations)


        //Faz o broadcast do intent
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}