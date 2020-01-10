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
    var dovizTr: String? = null
    val dbMoney by lazy { DBHelper(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)

        dolarVeriGetir()
        //dolarVeriGetir("EUR")
        //euroVeriGetir()

        btnSonKayit.setOnClickListener {
            val lastDolarDegeri=dbMoney.lastValue()
            Log.e("OSMAN","SON DOLAR DEGERİ BU MU ==="+lastDolarDegeri)
        }

        btnDelete.setOnClickListener {
            dbMoney.deleteAllData()
        }
        btnRead.setOnClickListener {
           showData(dbMoney.readData())
        }

    }

    fun dolarVeriGetir() {

        val dolarUrl = "https://api.exchangeratesapi.io/latest?base=USD"
        val dovizObje = JsonObjectRequest(
            Request.Method.GET,
            dolarUrl,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {

                    var rates = response?.getJSONObject("rates")
                    dovizTr = rates?.getString("TRY")

                    var bosMu:Boolean?=null
                    bosMu=dbMoney.isEmptyTable()

                    if (bosMu == false) // F A L S E    İ S E   D O L U D U R    D İ Ğ E R   K O N T R O L L E R   Y A P I L M A L I D I R
                    {

                        //Log.e("OSMAN","Deneme bool boş mu dolu mu ???"+ denemeBool)


                    }else{  // T R U E  B O Ş  İ S E    Y A P I L A C A K L A R

                            dbMoney.insertData(ParaBirimleriTablo(dollar=dovizTr.toString()))
                            tvDolar.text = dovizTr.toString()




                    }


                   /*
                    var denemeBool:Boolean?=null
                    denemeBool=dbMoney.isEmptyTable()
                    Log.e("OSMAN","Deneme bool boş mu dolu mu ???"+ denemeBool)
                    */

                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {


                }
            })
        MySingleton.getInstance(this).addToRequestQueue(dovizObje)


    }

    fun euroVeriGetir() {
        val euroUrl = "https://api.exchangeratesapi.io/latest?base=EUR"
        val dovizObje2 = JsonObjectRequest(
            Request.Method.GET,
            euroUrl,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {

                    var rates = response?.getJSONObject("rates")
                    var dovizTr = rates?.getDouble("TRY")
                    //Log.e("Döviz","1 euro kaç TL? :"+dovizTr)
                    tvEuro.text = dovizTr.toString()

                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {


                }
            })
        MySingleton.getInstance(this).addToRequestQueue(dovizObje2)
    }



    fun showData(list: MutableList<ParaBirimleriTablo>) {
        txtDenemeMedium.text = ""
        list.forEach {
            txtDenemeMedium.text =
                txtDenemeMedium.text.toString() + "\n" + it.dollar + " " + it.euro + " " + it.date
        }
    }
}
