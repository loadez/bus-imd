package dev.loadez.bus.application

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import dev.loadez.bus.domain.AgencyModel
import dev.loadez.bus.domain.BusModel
import dev.loadez.bus.domain.LocationModel
import dev.loadez.bus.infraestructure.DbAdapter

object AgencyDAO {
    fun insert(agency: AgencyModel): Long {
        val values = ContentValues()
        values.put("id" , agency.id)
        values.put("agency_id" , agency.agencyId)
        values.put("name" , agency.name)
        Log.d("TAG", "insert: $agency ")
        return DbAdapter.writeDb!!.insertWithOnConflict("agency",null,values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun insertAll(agencies:List<AgencyModel>): List<AgencyModel> {
        val result  = mutableListOf<AgencyModel>()
        DbAdapter.writeDb!!.beginTransaction()
        try {
            for (a in agencies){
                val id = insert(a).toInt()
                if(id!=-1){
                    result.add(
                        a.copy(id = id)
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


    fun getALl():List<AgencyModel>{
        val result  = mutableListOf<AgencyModel>()
        with( DbAdapter.readDb!!.rawQuery("""SELECT * FROM agency;""",null)){
            while (this.moveToNext()){
                result.add(
                    AgencyModel(
                        getInt(0),
                        getString(1),
                        getString(2),
                    )
                )
            }
        }
        return result
    }
}