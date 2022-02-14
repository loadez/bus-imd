package dev.loadez.bus.application

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import dev.loadez.bus.domain.BusModel
import dev.loadez.bus.infraestructure.DbAdapter


object BusDAO {

    fun insert(bus:BusModel): Long {
        val values = ContentValues()
        values.put("id" , bus.id)
        values.put("plate" , bus.plate)
        values.put("label" , bus.label)
        values.put("vehicle_id" , bus.vehicle_id)
        Log.d("TAG", "insert: $bus ")
        return DbAdapter.writeDb!!.insertWithOnConflict("bus",null,values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun getId(bus:BusModel):BusModel{
        with(DbAdapter.readDb!!.rawQuery("""SELECT * FROM bus WHERE plate="${bus.plate}" AND label="${bus.label}" AND vehicle_id="${bus.vehicle_id}";""",null)){
            return if (this.moveToFirst()){
                val id = this.getInt(0)
                bus.copy(id = id)
            } else{
                bus
            }
        }
    }

    fun insertAll(buses:List<BusModel>): List<BusModel> {
        val result  = mutableListOf<BusModel>()
        val toGet  = mutableListOf<BusModel>()
        DbAdapter.writeDb!!.beginTransaction()
        try {
            for (b in buses){
                val id = insert(b).toInt()
                if (id==-1){
                    toGet.add(b)
                }
                else{
                    result.add(
                        b.copy(id = id)
                    )
                }
            }
            DbAdapter.writeDb!!.setTransactionSuccessful()
        }
        finally {
            DbAdapter.writeDb!!.endTransaction()
        }
        for (b in toGet){
            result.add(getId(b))
        }
        return result
    }
}