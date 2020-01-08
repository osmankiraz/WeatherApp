package com.example.havadurumuapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_currency.*
import org.json.JSONObject
import java.lang.Exception
import java.lang.Thread.sleep

class CurrencyActivity : AppCompatActivity() {
    var dovizTr:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)






        dolarVeriGetir()
        euroVeriGetir()




    }

    fun dolarVeriGetir(){

        val dolarUrl="https://api.exchangeratesapi.io/latest?base=USD"
        val dovizObje=JsonObjectRequest(Request.Method.GET,dolarUrl,null,object : Response.Listener<JSONObject>{
            override fun onResponse(response: JSONObject?) {

                var rates=response?.getJSONObject("rates")
                dovizTr=rates?.getString("TRY")

                tvDolar.text=dovizTr.toString()///////////////////////////////////////////////////////////////////////////////////////

                try {

                    var dollarDatabase=this@CurrencyActivity.openOrCreateDatabase("Dollar",Context.MODE_PRIVATE,null)
                    dollarDatabase.execSQL("CREATE TABLE IF NOT EXISTS dollar(id INTEGER PRIMARY KEY,deger VARCHAR)")
                    //dollarDatabase.execSQL("INSERT INTO dollar(deger) VALUES($dovizTr)")
                   // dollarDatabase.execSQL("DELETE FROM dollar")


                    Log.e("Döviz","///////"+"dovizTr:"+dovizTr)

                    val cursor=dollarDatabase.rawQuery("SELECT * FROM dollar",null)
                    val dollarIx=cursor.getColumnIndex("deger")
                    val idIx=cursor.getColumnIndex("id")

                    while (cursor.moveToNext()){
                        Log.e("Döviz","id : "+cursor.getInt(idIx) )
                        Log.e("Döviz","dolar : "+cursor.getString(dollarIx) )

                    }
                    cursor.moveToLast()
                    Log.e("Döviz","dolarLAST : "+cursor.getString(dollarIx) )

                    cursor.close()
                }catch (e:Exception){
                    e.printStackTrace()
                    Log.e("Döviz","Hata mesajı e : "+e.printStackTrace())
                }




            }
        },object:Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {


            }
        })
        MySingleton.getInstance(this).addToRequestQueue(dovizObje)



    }


    // ç ö p
    fun veritabanıIslemi(){


        try {

            var myDatabase=this.openOrCreateDatabase("Doviz", Context.MODE_PRIVATE,null)
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS doviz(id INTEGER PRIMARY KEY,euro VARCHAR,dolar VARCHAR)")

            var kaydetmelikStr=dovizTr
            Log.e("Döviz","kaydetmelikStr:"+kaydetmelikStr+"///////"+"dovizTr:"+dovizTr)
            // myDatabase.execSQL("INSERT INTO doviz(dolar,euro) VALUES('dovizTr','ali')")
            //myDatabase.execSQL("INSERT INTO doviz(dolar,euro) VALUES($kaydetmelikStr,'mehmet')")

            val cursor=myDatabase.rawQuery("SELECT * FROM doviz",null)
            val dolarIx=cursor.getColumnIndex("dolar")
            val euroIx=cursor.getColumnIndex("euro")
            val idIx=cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                Log.e("Döviz","dolar sql ile geldi : "+cursor.getString(dolarIx))
                Log.e("Döviz","euro sql ile geldi : "+cursor.getString(euroIx))
                Log.e("Döviz","id sql ile geldi : "+cursor.getInt(idIx))
            }
            cursor.close()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e("Döviz","Hata mesajı e : "+e.printStackTrace())
        }




    }
    // ç ö p



    fun euroVeriGetir(){
        val euroUrl="https://api.exchangeratesapi.io/latest?base=EUR"
        val dovizObje2=JsonObjectRequest(Request.Method.GET,euroUrl,null,object : Response.Listener<JSONObject>{
            override fun onResponse(response: JSONObject?) {

                var rates=response?.getJSONObject("rates")
                var dovizTr=rates?.getDouble("TRY")
                //Log.e("Döviz","1 euro kaç TL? :"+dovizTr)
                tvEuro.text=dovizTr.toString()

            }
        },object:Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {


            }
        })
        MySingleton.getInstance(this).addToRequestQueue(dovizObje2)
    }
}
