package dev.loadez.bus.application

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import dev.loadez.bus.domain.LocationModel
import dev.loadez.bus.infraestructure.DbAdapter

object LocationDAO {

    fun insert(location: LocationModel): Long {
        val values = ContentValues()
        values.put("id" , location.id)
        values.put("vehicle_id" , location.vehicle_id)
        values.put("latitude" , location.latitude)
        values.put("longitude" , location.longitude)
        values.put("timestamp" , location.timestamp)
        Log.d("TAG", "insert: $location ")
        return DbAdapter.writeDb!!.insertWithOnConflict("location",null,values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun insertAll(locations:List<LocationModel>): List<LocationModel> {
        val result  = mutableListOf<LocationModel>()
        DbAdapter.writeDb!!.beginTransaction()
        try {
            for (l in locations){
                val id = insert(l).toInt()
                if(id!=-1){
                    result.add(
                        l.copy(id = id)
                    )
                }
            }
            DbAdapter.writeDb!!.setTransactionSuccessful()
        }
        finally {
            DbAdapter.writeDb!!.endTransaction()
        }
        return result
    }
}