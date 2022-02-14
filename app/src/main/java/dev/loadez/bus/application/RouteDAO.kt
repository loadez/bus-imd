package dev.loadez.bus.application

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import dev.loadez.bus.domain.AgencyModel
import dev.loadez.bus.domain.RouteModel
import dev.loadez.bus.infraestructure.DbAdapter

object RouteDAO {
    fun insert(route: RouteModel): Long {
        val values = ContentValues()
        values.put("id" , route.id)
        values.put("route_id" , route.routeId)
        values.put("agency_id" , route.agencyId)
        values.put("route_short_name" , route.shortName)
        values.put("route_long_name" , route.longName)
        Log.d("TAG", "insert: $route ")
        return DbAdapter.writeDb!!.insertWithOnConflict("route",null,values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun insertAll(routes:List<RouteModel>): List<RouteModel> {
        val result  = mutableListOf<RouteModel>()
        DbAdapter.writeDb!!.beginTransaction()
        try {
            for (r in routes){
                val id = insert(r).toInt()
                if(id!=-1){
                    result.add(
                        r.copy(id = id)
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