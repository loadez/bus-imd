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
import dev.loadez.bus.domain.BusModel
import dev.loadez.bus.domain.GtfsRealtime
import java.lang.Exception
import java.net.HttpURLConnection
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
        val notification = Notification()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel("BusUpdate","BusUpdate",NotificationManager.IMPORTANCE_LOW))
        }
        val builder = NotificationCompat.Builder(this,"BusUpdate")


        startForeground(1,builder.build())


        //Cria o thread de trabalho
        workThread = Thread{
            while (shouldRun){

                try {
                    Thread.sleep(10000)
                    Log.d("TAG", "onCreate: RODEI!")

                    val url =
                        URL("https://s3-sa-east-1.amazonaws.com/dados.natal.br/FeedMessage.pb")
                    val bytes = url.readBytes()


                    val feed = GtfsRealtime.FeedMessage.parseFrom(bytes)

                    //Cria a lista de atualizações
                    val buses = feed.entityList.map {
                        BusModel(
                            "${it.vehicle.vehicle.id}@${it.vehicle.vehicle.label}",
                            it.vehicle.position.latitude.toDouble(),
                            it.vehicle.position.longitude.toDouble(),
                            "",
                            it.vehicle.trip.routeId,
                        )

                    }.toTypedArray()





                    sendBusesUpdate(buses)
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

    private fun sendBusesUpdate(buses : Array<BusModel>){
        //Cria o intent para ser passado
        val intent = Intent("BusesUpdate")

        //Adiciona ela como um extra
        intent.putExtra("Buses",buses)


        //Faz o broadcast do intent
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}