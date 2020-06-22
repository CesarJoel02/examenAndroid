package com.cesar.joel.gurrola.Util

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.cesar.joel.gurrola.model.DataClass

class DataBaseHelper (context: Context, DB_NAME :String, DB_VERSION: Int):
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){
    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = "CREATE TABLE $TABLE_NAME " +
                "($ID Integer PRIMARY KEY, $DETERMINATE TEXT, $CADENA TEXT, $SUCURSAL TEXT, $LATITUD TEXT, $LONGITUD TEXT"
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun addData(dataClass: DataClass): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(DETERMINATE, dataClass.determinate)
        values.put(CADENA, dataClass.Cadena)
        values.put(SUCURSAL, dataClass.Sucursal)
        values.put(LATITUD, dataClass.latitud)
        values.put(LONGITUD, dataClass.longitud)
        val _success = db.insert(TABLE_NAME, null, values)
        db.close()
        Log.v("InsertedID", "$_success")
        return (Integer.parseInt("$_success") != -1)
    }

    companion object {
        private val DB_NAME = "DataDB"
        private val DB_VERSIOM = 1;
        private val TABLE_NAME = "data"
        private val ID = "id"
        private val DETERMINATE = "Determinate"
        private val CADENA = "Cadena"
        private val SUCURSAL = "Sucursal"
        private val LATITUD = "Latitud"
        private val LONGITUD = "Loongitud"
    }
}

//determinante, Cadena, sucursal, latitud y longitud