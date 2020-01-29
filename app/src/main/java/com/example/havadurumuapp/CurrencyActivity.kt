package com.example.havadurumuapp

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.example.havadurumuapp.DB.DBEuroHelper
import com.example.havadurumuapp.DB.DBHelper
import com.example.havadurumuapp.DB.MySingleton
import com.example.havadurumuapp.Model.EuroTablo
import com.example.havadurumuapp.Model.ParaBirimleriTablo
import kotlinx.android.synthetic.main.activity_currency.*
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import maes.tech.intentanim.CustomIntent.customType

class CurrencyActivity : AppCompatActivity() {
    var dovizTr: String? = null
    var apiDateStr: String? = null
    var apiEDateStr: String? = null
    var currentDateTime: String? = null
    val dbMoney by lazy { DBHelper(this) }
    val dbEuro by lazy { DBEuroHelper(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)
        currentDateTime = tarihYazdir()
        dolarVeriGetir("USD")
        euroVeriGetir()
    }

    fun dolarVeriGetir(currencyUnit: String) {

        // kullanıcının internete bağlı olup olmadığını anlamak için kullanığım değer
        val webLoaderThread = Thread {
            if (MainActivity.MyReachability.hasInternetConnected(this)){
                runOnUiThread {}
            } else {
                runOnUiThread {
                    // kullanıcı internet verisini açması için alert diaglog
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Internet Yok")
                    builder.setIcon(R.drawable.wifi)
                    builder.setPositiveButton("Ayarlar",object : DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            val intent= Intent()
                            intent.setAction(Settings.ACTION_WIRELESS_SETTINGS)     // wireless ayarlarına gönderim
                            startActivity(intent)
                            customType(this@CurrencyActivity,"fadein-to-fadeout") // intent animation
                            finish()
                        }
                    })
                    // close ve settings icon atamaları
                    var draw: Drawable =resources.getDrawable(R.drawable.ic_close_black_24dp)
                    var drawSet: Drawable =resources.getDrawable(R.drawable.ic_settings_black_24dp)
                    builder.setNegativeButtonIcon(draw)
                    builder.setPositiveButtonIcon(drawSet)
                    builder.setMessage("İnternet Bağlantınızı Açmalısınız")
                        .setNegativeButton("KAPAT",object : DialogInterface.OnClickListener{
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                finish()// akitivity bitiş
                            }
                        }).show()
                }
            }
        }
        webLoaderThread.start()

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
                        var lastDBDate: String = dbMoney.lastDateValue()
                        // NORMALDE SON APİ DATE İLE CURRENTTİME A  EŞİT OLDUĞUNU KONTROL EDİP DENEMELERİ YAPICAKTIM AMA
                        // APİ DATE İ HİÇ BİR ZAMAN CURRENTTİME A EŞİT DEĞİL. KULLANDIĞIM APİ SON İŞ GÜNÜNÜ VERMEKTE O YÜZDEN
                        // DATEBASEDE Kİ SON KAYDEDİLEN VERİNİN DATE İLE CURRENT TİME KONTROL EDEREK DEVAM EDİYORUM
                        if (currentDateTime == lastDBDate /*apiDateStr*/) {

                            // T A R İ H L E R   E Ş İ T   İ S E   S O N   V E R İ Y İ     Y A  Z D I R
                            var sonDolarDeger = dbMoney.lastValue()
                            tvDolar.text = sonDolarDeger
                            Log.e("OSMAN", " TARİHLER EŞİT DOLAR ICIN ")
                        } else { //  T A R İ H L E R   E Ş İ T    D E Ğ İ L     İ S E
                            Log.e("OSMAN", " TARİHLER EŞİT DEĞİL DOLAR ICIN ")
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
                override fun onErrorResponse(error: VolleyError?) {}
            })
        MySingleton.getInstance(this).addToRequestQueue(dovizObje)// sorgu bitiş
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
                    var jsonDate = response?.getString("date")
                    apiEDateStr = jsonDate
                    var bosMu: Boolean? = null
                    bosMu = dbEuro.isEmptyEuroTable()
                    Log.e("OSMAN", "EURO TABLOSU BOŞ mu  ===" + bosMu.toString())

                    if (bosMu == false) {  // F A L S E    İ S E   D O L U D U R    D İ Ğ E R   K O N T R O L L E R   Y A P I L M A L I D I R
                        var lastDBDate: String = dbEuro.lastEuroDateValue()
                        if (currentDateTime == lastDBDate) {
                            // T A R İ H L E R   E Ş İ T   İ S E   S O N   V E R İ Y İ     Y A  Z D I R
                            Log.e("OSMAN", " TARİHLER EŞİT EURO ICIN ")
                            var sonEuroDeger = dbEuro.lastEuroValue()
                            tvEuro.text = sonEuroDeger
                        } else {//  T A R İ H L E R   E Ş İ T    D E Ğ İ L     İ S E
                            Log.e("OSMAN", " TARİHLER EŞİT DEĞİL EURO ICIN ")
                            dbEuro.insertEuroData(
                                EuroTablo(
                                    euro = dovizEuroTr.toString(),
                                    date = currentDateTime.toString()
                                )
                            )
                            var sonEuroDeger = dbEuro.lastEuroValue()
                            tvEuro.text = sonEuroDeger
                        }
                    } else {// T R U E  B O Ş  İ S E    Y A P I L A C A K L A R
                        dbEuro.insertEuroData(
                            EuroTablo(
                                euro = dovizEuroTr.toString(),
                                date = currentDateTime.toString()
                            )
                        )
                        var euroSonDeger = dbEuro.lastEuroValue()
                        tvEuro.text = euroSonDeger
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {}
            })
        MySingleton.getInstance(this).addToRequestQueue(dovizObje2)
    }

    // ANLIK TARİH VERİSİNİ DBYE YAZARKEN 2020-02-02 FORMATINDA BANA DÖNDÜREN FONKSİYON
    fun tarihYazdir(): String {
        var currentTime = LocalDateTime.now()
        //val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("tr"))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("tr"))
        val formattedCurrentTime = currentTime.format(formatter)
        return formattedCurrentTime
    }

    // GERİYE BASILDIĞINDA ANİMASYON TETİKLEMEK İÇİN ON BACK PRESSED OVERRİDE EDİLDİ
    override fun onBackPressed() {
        super.onBackPressed()
        customType(this@CurrencyActivity,"left-to-right")// İNTENT ANİMATİON
    }

}
