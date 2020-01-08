package com.example.havadurumuapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_currency.*
import org.json.JSONObject

class CurrencyActivity : AppCompatActivity() {

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
                var dovizTr=rates?.getDouble("TRY")
                tvDolar.text=dovizTr.toString()

                Log.e("Döviz","1 dolar kaç TL? :"+dovizTr)


            }
        },object:Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {


            }
        })
        MySingleton.getInstance(this).addToRequestQueue(dovizObje)

    }

    fun euroVeriGetir(){
        val euroUrl="https://api.exchangeratesapi.io/latest?base=EUR"
        val dovizObje2=JsonObjectRequest(Request.Method.GET,euroUrl,null,object : Response.Listener<JSONObject>{
            override fun onResponse(response: JSONObject?) {

                var rates=response?.getJSONObject("rates")
                var dovizTr=rates?.getDouble("TRY")
                Log.e("Döviz","1 euro kaç TL? :"+dovizTr)
                tvEuro.text=dovizTr.toString()

            }
        },object:Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {


            }
        })
        MySingleton.getInstance(this).addToRequestQueue(dovizObje2)
    }
}
