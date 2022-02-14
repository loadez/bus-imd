package dev.loadez.bus.infraestructure

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

object DbAdapter {
    private var initialized = false
    private lateinit var dbHelper: DatabaseHelper

    var readDb: SQLiteDatabase? = null
        get() = dbHelper.readableDatabase
    var writeDb: SQLiteDatabase? = null
        get() = dbHelper.writableDatabase

    fun initializeDatabase(context: Context){
        synchronized(this){
            if (!initialized){
                dbHelper = DatabaseHelper(context)
                initialized= true
            }
        }
    }
}

private class DatabaseHelper :SQLiteOpenHelper{
    constructor(context: Context) : super(context,"default",null,1) {
    }

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            db.execSQL("""CREATE TABLE "bus" (	"id"	INTEGER,	"plate"	TEXT,	"label"	TEXT,	"vehicle_id"	TEXT,	UNIQUE("vehicle_id","label","plate"),	PRIMARY KEY("id" ASC))""")
            db.execSQL("""CREATE TABLE "location" ("id"	INTEGER,"vehicle_id"	INTEGER NOT NULL,"latitude"	REAL NOT NULL,"longitude"	REAL NOT NULL,"timestamp"	INTEGER NOT NULL,FOREIGN KEY("vehicle_id") REFERENCES "bus"("id"),UNIQUE("vehicle_id","timestamp"),PRIMARY KEY("id" ASC));""")
            db.execSQL("""CREATE TABLE "agency" (	"id"	INTEGER,	"agency_id"	TEXT NOT NULL UNIQUE,	"name"	TEXT NOT NULL,	PRIMARY KEY("id" ASC));""")
            db.execSQL("""CREATE TABLE "route" (	"id"	INTEGER,	"route_id"	TEXT UNIQUE,	"agency_id"	INTEGER NOT NULL,	"route_short_name"	TEXT NOT NULL,	"route_long_name"	TEXT NOT NULL,	PRIMARY KEY("id" ASC));""")


        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

}