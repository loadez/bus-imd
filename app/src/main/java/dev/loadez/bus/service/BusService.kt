package dev.loadez.bus.service;

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service;
import android.content.Intent;
import android.os.Build
import android.os.IBinder;
import android.util.Log

import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.loadez.bus.domain.BusModel

public class BusService : Service() {
    private  lateinit var workThread: Thread
    private var shouldRun = true

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY;
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
                Thread.sleep(1000);
                Log.d("TAG", "onCreate: RODEI!")
                sendBusesUpdate()
            }
        }

        //Algumas configurações do thread
        workThread.isDaemon = true;
        workThread.priority = 4;
        workThread.start()
    }

    private fun sendBusesUpdate(){
        //Cria o intent para ser passado
        val intent = Intent("BusesUpdate")

        //Cria a lista de atualizações
        val buses = mutableListOf<BusModel>();
        buses.add(BusModel("23_345",0.0,0.0,"",""));

        //Adiciona ela como um extra
        intent.putExtra("Buses",buses.toTypedArray())
        val t = buses.toTypedArray()
        t

        //Faz o broadcast do intent
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}