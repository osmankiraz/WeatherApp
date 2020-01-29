package com.example.havadurumuapp.DB

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.havadurumuapp.Model.ParaBirimleriTablo


// FAZLADAN CLASS OLUŞTURMAMIN SEBEBİ İLK BAŞTAKİ TEKLİ OLUŞTURDUĞUM DOLAR TABLOLARINDAN  SONRA ONCREATE ALTINDA EURO
// TABLOSU OLUŞTURAMADIĞIM İÇİNDİ. DAHA SONRADAN ÖĞRENDİMKİ UYGULAMAYI KOMPLE SİLİP TEKRAR BAŞLATTIĞIMDA ASLINDA EURO
// TABLOSU OLUŞTURULACAKTI.AMA BEN YENİ SINIF AÇTIM.
// ÖYLE YAPMIŞ OLSAYDIM BÜTÜN BU FONKSİYONLARI TEKRAR TEKRAR COPY PASTE YAPMADAN PARAMETRE ALARAK BİR KEREDE HALLETMİŞ
// OLACAKTIM.
class DBHelper(val context: Context) :
    SQLiteOpenHelper(context,
        DATABASE_NAME, null,
        DATABASE_VERSION
    ) {
    companion object {
        private val DATABASE_NAME = "SQLITE_DATABASE"   //database adı
        private val DATABASE_VERSION = 1
    }
    // D O L L A R    T A B L O  D E G E R L E R İ
    private val TABLE_NAME = "Currency"
    private val COL_ID = "id"
    private val COL_DOLAR = "dollar"
    private val COL_EURO = "euro"
    private val COL_DATE = "date"

    override fun onCreate(db: SQLiteDatabase?) {
        // DOLAR TABLOSU OLUŞTURMA
        val createTable = "CREATE TABLE $TABLE_NAME ($COL_ID  INTEGER PRIMARY KEY AUTOINCREMENT,$COL_DOLAR VARCHAR ,$COL_EURO VARCHAR ,$COL_DATE VARCHAR ) "
        db?.execSQL(createTable)
    }
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}
    // V E R İ   E K L E M E
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

    // T A B L O     B O Ş  M U
    fun isEmptyTable():Boolean{
        var booleanEmpty=true
        val db=this.readableDatabase
        val cursor=db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME",null)
        if(cursor !=null && cursor.moveToFirst()){
            booleanEmpty=(cursor.getInt(0)==0)
        }
        cursor.close()
        return booleanEmpty
    }

    // S O N     D E G E R   D E N E M E
    fun lastValue():String{
        val db =this.readableDatabase
        var lastValueStr=""
        val strQuery="SELECT * FROM $TABLE_NAME ORDER BY $COL_ID DESC LIMIT 1"
        val cursor=db.rawQuery(strQuery,null)
        val lastDollarIx=cursor.getColumnIndex(COL_DOLAR)
        if(cursor != null && cursor.moveToLast()){
            lastValueStr=cursor.getString(lastDollarIx)
        }
        return lastValueStr
    }

    // S O N  T A R İ H
    fun lastDateValue():String{
        val db =this.readableDatabase
        var lastDateValueStr=""
        val strQuery="SELECT * FROM $TABLE_NAME ORDER BY $COL_ID DESC LIMIT 1"
        val cursor=db.rawQuery(strQuery,null)
        val lastDollarIx=cursor.getColumnIndex(COL_DATE)
        if(cursor != null && cursor.moveToLast()){
            lastDateValueStr=cursor.getString(lastDollarIx)
        }
        return lastDateValueStr
    }

    // O K U M A
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

    //T Ü M ÜN Ü  S İ L
    fun deleteAllData() {

        val sqliteDB = this.writableDatabase
        sqliteDB.delete(TABLE_NAME, null, null)
        sqliteDB.close()
    }
}