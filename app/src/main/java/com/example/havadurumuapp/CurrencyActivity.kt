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

        dolarVeriGetir("USD")
        //dolarVeriGetir("EUR")
        //euroVeriGetir()


    }

    fun dolarVeriGetir(currencyUnit: String) {

        val dolarUrl = "https://api.exchangeratesapi.io/latest?base=" + currencyUnit
        val dovizObje = JsonObjectRequest(
            Request.Method.GET,
            dolarUrl,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {

                    var rates = response?.getJSONObject("rates")
                    dovizTr = rates?.getString("TRY")

                    if (currencyUnit == "USD") {
                        tvDolar.text = dovizTr.toString()
                    } else {
                        tvEuro.text = dovizTr.toString()
                    }

                    dbMoney.insertData(
                        ParaBirimleriTablo(
                            dollar = "5.66",
                            euro = "6.911",
                            date = "09012020"
                        )
                    )
                    showData(dbMoney.readData())
                    //Log.e("Doviz", "Medium deneme : " +)

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
