package com.example.havadurumuapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DBWeatherHelper(val context: Context) : SQLiteOpenHelper(
    context,
    DBWeatherHelper.DATABASE_NAME,
    null,
    DBWeatherHelper.DATABASE_VERSION
) {
    companion object {
        private val DATABASE_NAME = "SQLITE_DATABASE_WEATHER"   //database adı
        private val DATABASE_VERSION = 1
    }

    private val TABLE_NAME = "Weather"
    private val COL_ID = "id"
    private val COL_CITY = "city"
    private val COL_CG = "temp"
    private val COL_DESC = "description"
    private val COL_DATE = "date"
    private val COL_ICON = "icon"


    override fun onCreate(db: SQLiteDatabase?) {
        val createTable =
            "CREATE TABLE $TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,$COL_CITY VARCHAR,$COL_CG INT, $COL_DESC VARCHAR,$COL_DATE VARCHAR,$COL_ICON VARCHAR)"
        db?.execSQL(createTable)

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {


    }

    fun insertDataWH(weatherTablo: WeatherTablo) {
        val sqLiteDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_CITY, weatherTablo.city)
        contentValues.put(COL_CG, weatherTablo.temp)
        contentValues.put(COL_DESC, weatherTablo.description)
        contentValues.put(COL_DATE, weatherTablo.date)
        contentValues.put(COL_ICON, weatherTablo.icon)

        val result = sqLiteDB.insert(TABLE_NAME, null, contentValues)
        Toast.makeText(
            context,
            if (result != -1L) "Kayıt H-D Başarılı" else "Kayıt H-D yapılamadı.",
            Toast.LENGTH_SHORT
        ).show()


    }

    fun isEmptyTable(): Boolean {
        var isEmpty = true
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT (*) FROM $TABLE_NAME ", null)
        if (cursor != null && cursor.moveToFirst()) {
            isEmpty = (cursor.getInt(0) == 0)
        }
        cursor.close()
        return isEmpty
    }

    // LAST VALUE FONKSİYONUNA GEREK YOK DÖVİZ SİSTEMİNDE OLDUĞU GİBİ
    // SONRADAN BAK HATA OLABİLİR   !  ! ! !! ! ! !! ! !      !    !
    fun lastDateValueWH(): String {
        val db = this.readableDatabase
        var lastDateValueWH = ""
        val strQuery = "SELECT * FROM  $TABLE_NAME ORDER BY $COL_CITY DESC LIMIT 1 "
        val cursor = db.rawQuery(strQuery, null)
        val lastDateIx = cursor.getColumnIndex(COL_DATE)

        if (cursor != null && cursor.moveToLast()) {
            lastDateValueWH = cursor.getString(lastDateIx)
        }
        return lastDateValueWH
    }

    fun readData(): MutableList<WeatherTablo> {
        val weatherListesi = mutableListOf<WeatherTablo>()
        val sqliteDB = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val result = sqliteDB.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val weatherBirimleri = WeatherTablo()
                weatherBirimleri.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                weatherBirimleri.city = result.getString(result.getColumnIndex(COL_CITY))
                weatherBirimleri.temp = result.getString(result.getColumnIndex(COL_CG))
                weatherBirimleri.description = result.getString(result.getColumnIndex(COL_DESC))
                weatherBirimleri.date = result.getString(result.getColumnIndex(COL_DATE))
                weatherBirimleri.icon = result.getString(result.getColumnIndex(COL_ICON))
                weatherListesi.add(weatherBirimleri)
            } while (result.moveToNext())
        }
        result.close()
        sqliteDB.close()
        return weatherListesi


    }

    fun deleteAllData(){
        val sqliteDB=this.writableDatabase
        sqliteDB.delete(TABLE_NAME,null, null)
        sqliteDB.close()
    }

}