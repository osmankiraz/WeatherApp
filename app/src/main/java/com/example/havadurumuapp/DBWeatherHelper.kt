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
            "CREATE TABLE $TABLE_NAME ($COL_ID INTEGER PRIMARY KEY ,$COL_CITY VARCHAR,$COL_CG  VARCHAR, $COL_DESC VARCHAR,$COL_DATE VARCHAR,$COL_ICON VARCHAR)"
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

        sqLiteDB.close()

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

    // DOĞRU ÇALIŞMIYOR
    fun isAddCity(): Boolean {
        var isAdd = true
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT($COL_CITY) FROM $TABLE_NAME ", null)
        if (cursor != null && cursor.moveToFirst()) {
            isAdd = (cursor.getInt(0) == 0)
        }
        cursor.close()
        return isAdd
    }

    fun kacTane(cityName: String): Int {
        val db = this.readableDatabase
        var lastValue = 0
        val strQuery = "SELECT COUNT($COL_ID) FROM $TABLE_NAME WHERE city='$cityName'  "
        val cursor = db.rawQuery(strQuery, null)
        //val index=cursor.getColumnIndex(COL_ID)
        val index2 = 0

        if (cursor != null && cursor.moveToFirst()) {
            lastValue = cursor.getInt(index2)
        }
        cursor.close()
        db.close()
        return lastValue
    }


    // A R A N A N   Ş E H R İ N     İ S M İ N İ     D Ö N D Ü R E N     F O N K S İ Y O N
    fun findSelectedCity(cityName: String): String {
        val db = this.readableDatabase
        var selectedCityName = ""
        val strQuery = "SELECT $COL_CITY FROM $TABLE_NAME WHERE city='$cityName' "
        val cursor = db.rawQuery(strQuery, null)
        val cityIx = cursor.getColumnIndex(COL_CITY)

        if (cursor != null && cursor.moveToNext()) {
            selectedCityName = cursor.getString(cityIx)
        }

        return selectedCityName
    }

    // A R A N A N   Ş E H R İ N     A C I K L A M A S I N I      D Ö N D Ü R E N     F O N K S İ Y O N
    fun findSelectedCityDescription(cityName: String): String {
        val db = this.readableDatabase
        var selectedCityDescription = ""
        val strQuery = "SELECT $COL_DESC FROM $TABLE_NAME WHERE city='$cityName' "
        val cursor = db.rawQuery(strQuery, null)
        val descIx = cursor.getColumnIndex(COL_DESC)

        if (cursor != null && cursor.moveToNext()) {
            selectedCityDescription = cursor.getString(descIx)
        }
        return selectedCityDescription
    }

    // A R A N A N   Ş E H R İ N    D A T E     D Ö N D Ü R E N     F O N K S İ Y O N
    fun findSelectedCityDate(cityName: String): String {
        val db = this.readableDatabase
        var selectedCityDate = ""
        val strQuery = "SELECT $COL_DATE FROM $TABLE_NAME WHERE city='$cityName'"
        val cursor = db.rawQuery(strQuery, null)
        val dateIx = cursor.getColumnIndex(COL_DATE)

        if (cursor != null && cursor.moveToNext()) {
            selectedCityDate = cursor.getString(dateIx)
        }
        return selectedCityDate
    }

    // A R A N A N   Ş E H R İ N    I C O N     D Ö N D Ü R E N     F O N K S İ Y O N
    fun findSelectedCityIcon(cityName: String): String {
        val db = this.readableDatabase
        var selectedCityIcon = ""
        val strQuery = "SELECT $COL_ICON FROM $TABLE_NAME WHERE city='$cityName'"
        val cursor = db.rawQuery(strQuery, null)
        val iconIx = cursor.getColumnIndex(COL_ICON)

        if (cursor != null && cursor.moveToNext()) {
            selectedCityIcon = cursor.getString(iconIx)
        }
        return selectedCityIcon
    }

    // A R A N A N   Ş E H R İ N     S I C A K L I Ğ I N I    D Ö N D Ü R E N     F O N K S İ Y O N
    fun findSelectedCityTemp(cityName: String): String {
        val db = this.readableDatabase
        var selectedCityTemp = ""
        val strQuery = "SELECT $COL_CG FROM $TABLE_NAME WHERE city='$cityName'"
        val cursor = db.rawQuery(strQuery, null)
        val tempIx = cursor.getColumnIndex(COL_CG)

        if (cursor != null && cursor.moveToNext()) {
            selectedCityTemp = cursor.getString(tempIx)
        }
        db.close()
        cursor.close()
        return selectedCityTemp
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

    fun deleteAllData() {
        val sqliteDB = this.writableDatabase
        sqliteDB.delete(TABLE_NAME, null, null)
        sqliteDB.close()
    }

    fun deleteSelectedCity(cityName: String) {
        val sqliteDB = this.writableDatabase
        val sqlQuery = "DELETE FROM $TABLE_NAME WHERE city='$cityName'"
        sqliteDB.execSQL(sqlQuery)
        sqliteDB.close()

    }


}