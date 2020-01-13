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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CurrencyActivity : AppCompatActivity() {
    var dovizTr: String? = null
    var apiDateStr: String? = null
    var apiEDateStr: String? = null
    var currentDateTime: String? = null
    val dbMoney by lazy { DBHelper(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)

        currentDateTime = tarihYazdir()


        dolarVeriGetir("USD")
        //dolarVeriGetir("EUR")
        //euroVeriGetir()

        btnSonKayit.setOnClickListener {
            val lastDolarDegeri = dbMoney.lastValue()
            Log.e("OSMAN", "SON DOLAR DEGERİ BU MU ===" + lastDolarDegeri)
            Log.e("OSMAN", "CURRENT TİME DEĞİŞKENİ ===" + currentDateTime)
            Log.e("OSMAN", " APİ DATE VERİSİ KONTROL === " + apiDateStr)
            var lastDBDate:String=dbMoney.lastDateValue()
            Log.e("OSMAN", " SON DB DATE VERİSİ === " + lastDBDate)
        }

        btnDelete.setOnClickListener {
            dbMoney.deleteAllData()
        }
        btnRead.setOnClickListener {
            showData(dbMoney.readData())
        }

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

                    var jsonDate = response?.getString("date")
                    apiDateStr = jsonDate

                    var bosMu: Boolean? = null
                    bosMu = dbMoney.isEmptyTable()

                    if (bosMu == false) // F A L S E    İ S E   D O L U D U R    D İ Ğ E R   K O N T R O L L E R   Y A P I L M A L I D I R
                    {
                        var lastDBDate:String=dbMoney.lastDateValue()


                        // NORMALDE SON APİ DATE İLE CURRENTTİME A  EŞİT OLDUĞUNU KONTROL EDİP DENEMELERİ YAPICAKTIM AMA
                        // APİ DATE İ HİÇ BİR ZAMAN CURRENTTİME A EŞİT DEĞİL. KULLANDIĞIM APİ SON İŞ GÜNÜNÜ VERMEKTE O YÜZDEN
                        // DATEBASEDE Kİ SON KAYDEDİLEN VERİNİN DATE İLE CURRENT TİME KONTROL EDEREK DEVAM EDİYORUM
                        if (currentDateTime ==lastDBDate /*apiDateStr*/) {

                            // T A R İ H L E R   E Ş İ T   İ S E   S O N   V E R İ Y İ     Y A  Z D I R
                            var sonDolarDeger = dbMoney.lastValue()
                            tvDolar.text = sonDolarDeger
                            Log.e("OSMAN", " TARİHLER EŞİT  === " )


                        } else { //  T A R İ H L E R   E Ş İ T    D E Ğ İ L     İ S E
                            Log.e("OSMAN", " TARİHLER EŞİT DEĞİL   === ")
                            dbMoney.insertData(
                                ParaBirimleriTablo(
                                    dollar = dovizTr.toString(),
                                    euro = "",
                                    date = currentDateTime.toString()
                                )
                            )
                            var sonDolarDeger = dbMoney.lastValue()
                            tvDolar.text = sonDolarDeger


                        }

                    } else {  // T R U E  B O Ş  İ S E    Y A P I L A C A K L A R

                        dbMoney.insertData(
                            ParaBirimleriTablo(
                                dollar = dovizTr.toString(),
                                euro = "",
                                date = currentDateTime.toString()
                            )
                        )
                        var sonDolarDeger = dbMoney.lastValue()
                        tvDolar.text = sonDolarDeger
                    }
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
                    var dovizEuroTr = rates?.getDouble("TRY")
                    tvEuro.text = dovizEuroTr.toString()


                    var jsonDate=response?.getString("date")
                    apiEDateStr=jsonDate


                    var bosMu: Boolean? = null
                    bosMu = dbMoney.isEmptyTable()






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

    fun tarihYazdir(): String {


        var currentTime = LocalDateTime.now()
        //val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("tr"))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("tr"))
        val formattedCurrentTime = currentTime.format(formatter)

        return formattedCurrentTime

    }

}
