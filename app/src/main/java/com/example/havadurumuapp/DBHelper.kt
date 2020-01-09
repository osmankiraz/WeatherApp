package com.example.havadurumuapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DBHelper(val context: Context) :
    SQLiteOpenHelper(context, DBHelper.DATABASE_NAME, null, DBHelper.DATABASE_VERSION) {
    companion object {
        private val DATABASE_NAME = "SQLITE_DATABASE"   //database adı
        private val DATABASE_VERSION = 1
    }

    private val TABLE_NAME = "Currency"
    private val COL_ID = "id"
    private val COL_DOLAR = "dollar"
    private val COL_EURO = "euro"
    private val COL_DATE = "date"


    override fun onCreate(db: SQLiteDatabase?) {
        val createTable =
            "CREATE TABLE $TABLE_NAME ($COL_ID  INTEGER PRIMARY KEY AUTOINCREMENT,$COL_DOLAR VARCHAR,$COL_EURO VARCHAR ,$COL_DATE VARCHAR ) "
        db?.execSQL(createTable)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {


    }


    fun insertData(paraBirimleriTablo: ParaBirimleriTablo) {
        val sqLiteDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_DOLAR, paraBirimleriTablo.dollar)
        contentValues.put(COL_EURO, paraBirimleriTablo.euro)
        contentValues.put(COL_DATE, paraBirimleriTablo.date)

        val result = sqLiteDB.insert(TABLE_NAME, null, contentValues)
        Toast.makeText(
            context,
            if (result != -1L) "Kayıt Başarılı" else "Kayıt yapılamadı.",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun readData(): MutableList<ParaBirimleriTablo> {
        val paraListesi = mutableListOf<ParaBirimleriTablo>()
        val sqLiteDB = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val result = sqLiteDB.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val paraBirimleri = ParaBirimleriTablo()
                paraBirimleri.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                paraBirimleri.dollar = result.getString(result.getColumnIndex(COL_DOLAR))
                paraBirimleri.euro = result.getString(result.getColumnIndex(COL_EURO))
                paraBirimleri.date = result.getString(result.getColumnIndex(COL_DATE))
                paraListesi.add(paraBirimleri)

            } while (result.moveToNext())
        }

        result.close()
        sqLiteDB.close()
        return paraListesi
    }

    fun deleteAllData() {

        val sqliteDB = this.writableDatabase
        sqliteDB.delete(TABLE_NAME, null, null)
        sqliteDB.close()
    }


}