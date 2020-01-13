package com.example.havadurumuapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBEuroHelper(val context: Context):SQLiteOpenHelper(context,DBEuroHelper.DATABASE_NAME,null,DBEuroHelper.DATABASE_VERSION) {
    companion object {
        private val DATABASE_NAME = "SQLITE_DATABASE_EURO"   //database adı
        private val DATABASE_VERSION = 1
    }
    // E U R O
    private val TABLE_EURO="EuroTable"
    private val COLUMN_ID="id"
    private val COLUMN_EURO="euro"
    private val COLUMN_DATE="date"

    override fun onCreate(db: SQLiteDatabase?) {
        val createEuroTable="CREATE TABLE $TABLE_EURO($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$COLUMN_EURO VARCHAR, $COLUMN_DATE VARCHAR)"
        db?.execSQL(createEuroTable)

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    fun insertEuroData(euroTablosu:EuroTablo){
        var sqliteDB=this.writableDatabase
        val contentValues= ContentValues()
        contentValues.put(COLUMN_EURO,euroTablosu.euro)
        contentValues.put(COLUMN_DATE,euroTablosu.date)

        var result =sqliteDB.insert(TABLE_EURO,null,contentValues)
        Log.e("OSMAN",if (result != -1L) "Kayıt E Başarılı" else "Kayıt E yapılamadı.")
    }

    fun isEmptyEuroTable():Boolean{
        var booleanEmpty=true
        val db=this.readableDatabase
        val cursor=db.rawQuery("SELECT COUNT(*) FROM $TABLE_EURO",null)
        if(cursor != null && cursor.moveToFirst()){
            booleanEmpty=(cursor.getInt(0)==0)

        }
        cursor.close()
        return booleanEmpty
    }

    fun lastEuroValue():String{
        val db =this.readableDatabase
        var lastEuroValueStr=""
        var strQuery ="SELECT * FROM $TABLE_EURO ORDER BY $COLUMN_ID DESC LIMIT 1 "
        val cursor=db.rawQuery(strQuery,null)
        val lastEuroIx=cursor.getColumnIndex(COLUMN_EURO)
        if (cursor != null && cursor.moveToLast()){
            lastEuroValueStr=cursor.getString(lastEuroIx)
        }
        return lastEuroValueStr
    }

    fun lastEuroDateValue():String{
        val db =this.readableDatabase
        var lastEuroDateStr=""
        val strQuery="SELECT * FROM $TABLE_EURO ORDER BY $COLUMN_ID DESC LIMIT 1"
        val cursor = db.rawQuery(strQuery,null)
        val lastEuroIx=cursor.getColumnIndex(COLUMN_DATE)
        if(cursor != null && cursor.moveToLast()){
            lastEuroDateStr=cursor.getString(lastEuroIx)

        }
        return lastEuroDateStr

    }

    fun readEuroData():MutableList<EuroTablo>{
        val paraListesi= mutableListOf<EuroTablo>()
        val sqliteDB=this.readableDatabase
        val query="SELECT * FROM $TABLE_EURO"
        val result =sqliteDB.rawQuery(query,null)

        if(result.moveToFirst()){
            do {
                val euroBirimi=EuroTablo()
                euroBirimi.id=result.getString(result.getColumnIndex(COLUMN_ID)).toInt()
                euroBirimi.euro=result.getString(result.getColumnIndex(COLUMN_EURO))
                euroBirimi.date=result.getString(result.getColumnIndex(COLUMN_DATE))
                paraListesi.add(euroBirimi)

            }while (result.moveToNext())
        }
        result.close()
        sqliteDB.close()
        return paraListesi

    }


    fun deleteEuroAllData(){
        val sqliteDB=this.writableDatabase
        sqliteDB.delete(TABLE_EURO,null,null)
        sqliteDB.close()
    }


}