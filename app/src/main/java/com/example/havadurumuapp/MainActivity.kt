package com.example.havadurumuapp

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.havadurumuapp.Adapter.RecyclerViewAdapter
import com.example.havadurumuapp.DB.DBWeatherHelper
import com.example.havadurumuapp.DB.MySingleton
import com.example.havadurumuapp.Model.WeatherTablo
import com.example.havadurumuapp.Model.besGunHava
import com.rhexgomez.typer.roboto.TyperRoboto
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import maes.tech.intentanim.CustomIntent.customType
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var tvSehir: TextView? = null
    var location: SimpleLocation? = null
    var latitude: String? = null
    var longitude: String? = null
    var tersEdildiMi: Boolean = false
    lateinit var localizedName: String
    lateinit var locationKey: String
    lateinit var listHavaArray: ArrayList<besGunHava>

    val dbWeather by lazy { DBWeatherHelper(this) }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    //SPİNNERDAN SEÇİM YAPILMASI DURUMU
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        tvSehir = view as TextView
        // P O S İ T İ O N  0 => Ş U A N K İ   Ş E H İ R   Y A P I L A C A K L A R
        if (position == 0) {
            location = SimpleLocation(this)
            if (!location!!.hasLocationEnabled()) {
                spnSehirler.setSelection(1)
                Toast.makeText(this, "GPS AÇ", Toast.LENGTH_LONG).show()
                SimpleLocation.openSettings(this)
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        60
                    )
                } else {
                    location = SimpleLocation(this)
                    latitude = String.format("%.2f", location?.latitude)
                    longitude = String.format("%.2f", location?.longitude)
                    var latitudeInt = String.format("%s", location?.latitude?.toInt())
                    var longitudeInt = String.format("%s", location?.longitude?.toInt())
                    fetchingLocationKeyOankiSehir(latitudeInt, longitudeInt)
                    oAnkiSehriGetir(latitude, longitude)
                }
            }
        } else {// SPİNNERDAN SECİLEN SEHRİN VERİLERİNİN BASILDIĞI KISIM
            var secilenSehir = parent?.getItemAtPosition(position).toString()
            tvSehir = view as TextView
            verileriGetir(secilenSehir)
            fetchingLocationKey(secilenSehir)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listHavaArray = ArrayList<besGunHava>()
        // İTEM POSİTİON 0 A KOYA BİLİRSİN BELKİ BUNU
        // ACILIR BİR PENCERE CIKICAK ORADAKİ EDİTTEXTE GİRİLEN ŞEHİR VERİGETİR'E PARAMETRE OLARAK GELECEK AMA
        // O VERİYİ STRİNG.XMLE KAYDEDEMİYORUM TEKRARDAN RESOURCES İÇİNE KAYDEDİLMİYORMUŞ ONLY READ OLD. ICIN
        var spinnerAdapter =
            ArrayAdapter.createFromResource(this, R.array.sehirler, R.layout.spinner_tek_satir)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // ARROW COLOR CHANGE
        spnSehirler.getBackground().setColorFilter(
            getResources().getColor(R.color.colorPrimaryDark),
            PorterDuff.Mode.SRC_ATOP
        )
        spnSehirler.adapter = spinnerAdapter
        spnSehirler.setTitle("Şehir Seçiniz")
        spnSehirler.onItemSelectedListener =
            this // SPİNNERDAN BİŞEY SEÇİLDİĞİNDE BİR HAREKET OLMASI İÇİN BUNU YAZMAK ZORUNDAYIM
        spnSehirler.setSelection(1)// UYGULAMA AÇILDIĞINDA ANKARA H-D GELSİN
    }

    // KULLANICININ BULUNDUĞU KOORDİNATLARIN ANLIK OLARAK HAVA DURUMUNU GETİREN FUN.
    private fun oAnkiSehriGetir(lat: String?, longt: String?) {

        // web loader thread o an internete bağlılığını kontrol eden degerim
        val webLoaderThread = Thread {
            if (MyReachability.hasInternetConnected(this)){
                runOnUiThread {
                }
            } else {
                runOnUiThread {
                    // internete bağlanması için bir alert dialog oluşturdum, kullanıcının direk olarak wireless ayarlarına yönlendiriyorum
                    val builder =AlertDialog.Builder(this)
                    builder.setTitle("Internet Yok")
                    builder.setIcon(R.drawable.wifi)// başlığın iconu
                    builder.setPositiveButton("Ayarlar",object :DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            val intent=Intent()
                            intent.setAction(Settings.ACTION_WIRELESS_SETTINGS)// wireless ayarlarına gidiş
                            startActivity(intent)
                            customType(this@MainActivity,"fadein-to-fadeout")// geçiş animasyonu !
                            finish()
                        }
                    })
                    //alert dialog iconlarının kaynakları
                    var draw:Drawable=resources.getDrawable(R.drawable.ic_close_black_24dp)
                    var drawSet:Drawable=resources.getDrawable(R.drawable.ic_settings_black_24dp)
                    builder.setNegativeButtonIcon(draw)
                    builder.setPositiveButtonIcon(drawSet)
                    builder.setMessage("İnternet Bağlantınızı Açmalısınız")
                        .setNegativeButton("KAPAT",object :DialogInterface.OnClickListener{
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                finish()
                            }
                        }).show()
                }
            }
        }
        webLoaderThread.start()

        var sehirAdi: String? = null
        val sehirUrl =
            "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + longt + "&appid=e57955a0fa8d61bebd89532dd21f4c15&lang=tr&units=metric"
        val havaDurumuObje2 = JsonObjectRequest(
            Request.Method.GET,
            sehirUrl,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {
                    var main = response?.getJSONObject("main")
                    var sicaklik = main?.getInt("temp")
                    sehirAdi = response?.getString("name")
                    var weather = response?.getJSONArray("weather")
                    var aciklama = weather?.getJSONObject(0)?.getString("description")
                    var icon = weather?.getJSONObject(0)?.getString("icon")

                    var sehirVarmi: Boolean
                    sehirVarmi = dbWeather.isEmptyTable()
                    Log.e("OSMAN", "TABLO BOŞ İSE TRU GELECEK? ? = " + sehirVarmi)

                    var kacTaneSehir = 0
                    kacTaneSehir = dbWeather.kacTane(sehirAdi!!)
                    Log.e("OSMAN", sehirAdi + " ŞEHRİNDEN  DB'DE KAÇ TANE VAR ? = " + kacTaneSehir)

                    var strCityName = ""
                    strCityName = dbWeather.findSelectedCity(sehirAdi!!)
                    Log.e("OSMAN", "FIND SEHİR ADI GETİRTME ? = " + strCityName)

                    var strCityTemp = ""
                    strCityTemp = dbWeather.findSelectedCityTemp(sehirAdi!!)
                    Log.e("OSMAN", "FIND SEHİR SICAKLIĞI GETİRTME ? = " + strCityTemp)

                    var strCityDescription = ""
                    strCityDescription = dbWeather.findSelectedCityDescription(sehirAdi!!)
                    Log.e("OSMAN", "FIND SEHİR DESCRİPTION GETİRTME ? = " + strCityDescription)

                    var strCityDate = ""
                    strCityDate = dbWeather.findSelectedCityDate(sehirAdi!!)
                    Log.e("OSMAN", "FIND SEHİR DATE GETİRTME ? = " + strCityDate)

                    var strCityIcon = ""
                    strCityIcon = dbWeather.findSelectedCityIcon(sehirAdi!!)
                    Log.e("OSMAN", "FIND SEHİR ICON GETİRTME ? = " + strCityIcon)

                    //SEÇİLEN SEHİRDEN TABLODA KAÇ TANE VAR 1 Mİ 0 MI
                    if (kacTaneSehir == 1) { // 1 TANE İSE YAPILICAKLAR
                        // tabloya eklenen bulunacak ve değerler karşılaştırılıcak
                        //degerler aynı ise db deb yaz || api deki degerler yeni ise api yi db ye yaz || db den ekrana bas
                        Log.e("OSMAN", "SEÇİLEN ŞEHİR DB DE 1 TANE ÇIKTI")
                        //KARŞILAŞTIRMAYI YAPAN IF KOŞULLARI
                        if (strCityTemp.equals(sicaklik.toString()) && strCityDescription.equals(
                                aciklama
                            ) && strCityDate.equals(tarihYazdir2()) && strCityIcon.equals(icon)
                        ) {
                            Log.e("OSMAN", "KARŞILATIRMADA HEPSİ EŞİT ÇIKTI")
                            Log.e("OSMAN", "VERİLER SADECE DB DEN ALINARAK YAZILDI")

                            tvSehir?.setText(localizedName)
                            //tvSehir?.setText(sehirAdi)
                            tvSicaklik.text = strCityTemp
                            tvAciklama.text = strCityDescription
                            tvTarih.text = tarihYazdir()
                            geceGunduzIcon(icon)


                        } else {
                            // HERHANGİ BİR TANE VERİ APİDEN DEĞİŞTİĞSE O SATIRI SİL VE YENİ EKLE DB YE
                            //AYNI ŞEHİRDEN 2 TANE OLMASINI İSTEMİYORUM         YA 1 YA 0

                            Log.e("OSMAN", "KARŞILATIRMADA HEPSİ EŞİT ÇIKMADI")
                            dbWeather.deleteSelectedCity(sehirAdi.toString()) // ÖNCE ŞEHİR VERİLERİ SİLİNDİ

                            dbWeather.insertDataWH( // DB YE YENİ VERİLER EKLENDİ
                                WeatherTablo(
                                    city = sehirAdi.toString(),
                                    temp = sicaklik.toString(),
                                    description = aciklama.toString(),
                                    date = tarihYazdir2(),
                                    icon = icon.toString()
                                )
                            )
                            var yeniSehirAdi = dbWeather.findSelectedCity(sehirAdi.toString())
                            var yeniSehirSicaklik =
                                dbWeather.findSelectedCityTemp(sehirAdi.toString())
                            var yeniSehirAciklama =
                                dbWeather.findSelectedCityDescription(sehirAdi.toString())
                            var yeniSehirIcon = dbWeather.findSelectedCityIcon(sehirAdi.toString())

                            tvSehir?.setText(yeniSehirAdi)
                            tvSicaklik.text = yeniSehirSicaklik
                            tvAciklama.text = yeniSehirAciklama
                            tvTarih.text = tarihYazdir()
                            geceGunduzIcon(yeniSehirIcon)
                            Log.e("OSMAN", "YENİ VERİLER DBYE YAZILDI ARDINDAN EKRANA BASILDI")

                        }


                    } else if (kacTaneSehir == 0) {  // 0 TANE İSE YAPILICAKLAR
                        //tabloya eklenecek

                        Log.e("OSMAN", "SEÇİLEN ŞEHİR DB DE HİÇ ÇIKMADI")
                        dbWeather.insertDataWH(
                            WeatherTablo(
                                city = sehirAdi.toString(),
                                temp = sicaklik.toString(),
                                description = aciklama.toString(),
                                date = tarihYazdir2(),
                                icon = icon.toString()
                            )
                        )//tabloya eklenecek

                        var yeniSehirAdi2 = dbWeather.findSelectedCity(sehirAdi.toString())
                        var yeniSehirSicaklik2 = dbWeather.findSelectedCityTemp(sehirAdi.toString())
                        var yeniSehirAciklama2 =
                            dbWeather.findSelectedCityDescription(sehirAdi.toString())
                        var yeniSehirIcon2 = dbWeather.findSelectedCityIcon(sehirAdi.toString())

                        tvSehir?.setText(localizedName)
                        //tvSehir?.setText(yeniSehirAdi2)
                        tvSicaklik.text = yeniSehirSicaklik2
                        tvAciklama.text = yeniSehirAciklama2
                        tvTarih.text = tarihYazdir()
                        geceGunduzIcon(yeniSehirIcon2)
                        Log.e("OSMAN", "YENİ VERİLER DBYE YAZILDI ARDINDAN EKRANA BASILDI")
                    } else {  // SEHİRDEN DB DE 2 VEYA DAHA FAZLA OLMASI DURUMU !!! İSTENMEYEN DURUM
                        Log.e("OSMAN", "SEHİR DBYE 2 >= KAYDEDİLMİŞ")
                        dbWeather.deleteSelectedCity(sehirAdi.toString())
                        dbWeather.insertDataWH( // DB YE YENİ VERİLER EKLENDİ
                            WeatherTablo(
                                city = sehirAdi.toString(),
                                temp = sicaklik.toString(),
                                description = aciklama.toString(),
                                date = tarihYazdir2(),
                                icon = icon.toString()
                            )
                        )
                        var yeniSehirAdi3 = dbWeather.findSelectedCity(sehirAdi.toString())
                        var yeniSehirSicaklik3 = dbWeather.findSelectedCityTemp(sehirAdi.toString())
                        var yeniSehirAciklama3 =
                            dbWeather.findSelectedCityDescription(sehirAdi.toString())
                        var yeniSehirIcon3 = dbWeather.findSelectedCityIcon(sehirAdi.toString())

                        tvSehir?.setText(localizedName)
                        //tvSehir?.setText(yeniSehirAdi3)
                        tvSicaklik.text = yeniSehirSicaklik3
                        tvAciklama.text = yeniSehirAciklama3
                        tvTarih.text = tarihYazdir()
                        geceGunduzIcon(yeniSehirIcon3)
                        Log.e("OSMAN", "YENİ VERİLER DBYE YAZILDI ARDINDAN EKRANA BASILDI")
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                }
            })
        MySingleton.getInstance(this).addToRequestQueue(havaDurumuObje2)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // İZİN HALİ
        if (requestCode == 60) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                location = SimpleLocation(this)
                latitude = String.format("%.2f", location?.latitude)
                longitude = String.format("%.2f", location?.longitude)
                var latitudeInt = String.format("%s", location?.latitude?.toInt())
                var longitudeInt = String.format("%s", location?.longitude?.toInt())
                Log.e("KOR", "latitude" + latitude)
                Log.e("KOR", "longitude" + longitude)
                Log.e("KOR", "latitudeINT" + latitudeInt)
                Log.e("KOR", "longitudeINT" + longitudeInt)
                fetchingLocationKeyOankiSehir(latitudeInt, longitudeInt)
                oAnkiSehriGetir(latitude, longitude)
            } else {
                spnSehirler.setSelection(1)
                Toast.makeText(this, "GPS verisini açmalısın", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Ş E H İ R    V E R İ L E R İ N İ     G E T İ R E N   F O N K S İ Y O N
    fun verileriGetir(sehir: String) {

        // BES GÜNLÜK H-D İÇİN SCROLL İŞLEMİNİ BELİRTEN SCROLLUP ANİMASYONUNU BAŞLATMA
        scroolUpAnim.setAnimation(R.raw.animup)
        scroolUpAnim.loop(true)
        scroolUpAnim.playAnimation()

        // kullanıcının internete bağlı olup olmadığını kontrol eden thread degerim
        val webLoaderThread = Thread {
            if (MyReachability.hasInternetConnected(this)){
                runOnUiThread {
                }
            } else {
                runOnUiThread {
                    // KULLANICININ İNTERNETİ AÇMASI İÇİN ALERT DİALOG
                    val builder =AlertDialog.Builder(this)
                    builder.setTitle("Internet Yok")
                    builder.setIcon(R.drawable.wifi)// başlık icon
                    builder.setPositiveButton("Ayarlar",object :DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            val intent=Intent()
                            intent.setAction(Settings.ACTION_WIRELESS_SETTINGS)// kullanıcıyı wireless ayarlarına gönderme
                            startActivity(intent)
                            customType(this@MainActivity,"fadein-to-fadeout")// intent animation
                            finish()
                        }
                    })
                    // positive ve negative button icon atamaları
                    var draw:Drawable=resources.getDrawable(R.drawable.ic_close_black_24dp)
                    var drawSet:Drawable=resources.getDrawable(R.drawable.ic_settings_black_24dp)
                    builder.setNegativeButtonIcon(draw)
                    builder.setPositiveButtonIcon(drawSet)
                    builder.setMessage("İnternet Bağlantınızı Açmalısınız")
                        .setNegativeButton("KAPAT",object :DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            finish()// kapat denilince aktiviteyi bitiriyorum
                        }
                    }).show()
                }
            }
        }
        webLoaderThread.start()

        collapsingToolbar.title = sehir
        collapsingToolbar.apply {// collapsing toolbar açık ve kapanık iken başlığının textStyle ı
            setCollapsedTitleTypeface(TyperRoboto.ROBOTO_REGULAR)// typerRoboto dışarıdan text için aldığım bir kütüphane
            setExpandedTitleTypeface(TyperRoboto.ROBOTO_ITALIC)
        }
        collapsingToolbar.setCollapsedTitleTextColor(resources.getColor(R.color.snowBir))// başlık renk ataması

        val ankaraUrl =
            "https://api.openweathermap.org/data/2.5/weather?q=" + sehir + "&appid=e57955a0fa8d61bebd89532dd21f4c15&lang=tr&units=metric"
        val havaDurumuObje = JsonObjectRequest(
            Request.Method.GET,
            ankaraUrl,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {
                    var main = response?.getJSONObject("main")
                    var sicaklik = main?.getInt("temp")
                    var sehirAdi = response?.getString("name")

                    var weather = response?.getJSONArray("weather")
                    var aciklama = weather?.getJSONObject(0)?.getString("description")
                    var icon = weather?.getJSONObject(0)?.getString("icon")

                    var sehirVarmi: Boolean
                    sehirVarmi = dbWeather.isEmptyTable()
                    Log.e("OSMAN", "TABLO BOŞ İSE TRU GELECEK? ? = " + sehirVarmi)

                    var kacTaneSehir = 0
                    kacTaneSehir = dbWeather.kacTane(sehir)
                    Log.e("OSMAN", sehir + " ŞEHRİNDEN  DB'DE KAÇ TANE VAR ? = " + kacTaneSehir)

                    var strCityName = ""
                    strCityName = dbWeather.findSelectedCity(sehir)
                    Log.e("OSMAN", "FIND SEHİR ADI GETİRTME ? = " + strCityName)

                    var strCityTemp = ""
                    strCityTemp = dbWeather.findSelectedCityTemp(sehir)
                    Log.e("OSMAN", "FIND SEHİR SICAKLIĞI GETİRTME ? = " + strCityTemp)

                    var strCityDescription = ""
                    strCityDescription = dbWeather.findSelectedCityDescription(sehir)
                    Log.e("OSMAN", "FIND SEHİR DESCRİPTION GETİRTME ? = " + strCityDescription)

                    var strCityDate = ""
                    strCityDate = dbWeather.findSelectedCityDate(sehir)
                    Log.e("OSMAN", "FIND SEHİR DATE GETİRTME ? = " + strCityDate)

                    var strCityIcon = ""
                    strCityIcon = dbWeather.findSelectedCityIcon(sehir)
                    Log.e("OSMAN", "FIND SEHİR ICON GETİRTME ? = " + strCityIcon)

                    //SEÇİLEN SEHİRDEN TABLODA KAÇ TANE VAR 1 Mİ 0 MI
                    if (kacTaneSehir == 1) { // 1 TANE İSE YAPILICAKLAR
                        // tabloya eklenen bulunacak ve değerler karşılaştırılıcak
                        //degerler aynı ise db deb yaz || api deki degerler yeni ise api yi db ye yaz || db den ekrana bas
                        Log.e("OSMAN", "SEÇİLEN ŞEHİR DB DE 1 TANE ÇIKTI")
                        //KARŞILAŞTIRMAYI YAPAN IF KOŞULLARI
                        if (strCityTemp.equals(sicaklik.toString()) && strCityDescription.equals(
                                aciklama
                            ) && strCityDate.equals(tarihYazdir2()) && strCityIcon.equals(icon)
                        ) {
                            Log.e("OSMAN", "KARŞILATIRMADA HEPSİ EŞİT ÇIKTI")
                            Log.e("OSMAN", "VERİLER SADECE DB DEN ALINARAK YAZILDI")
                            tvSicaklik.text = strCityTemp
                            tvAciklama.text = strCityDescription
                            tvTarih.text = tarihYazdir()
                            geceGunduzIcon(icon)
                        } else {
                            // HERHANGİ BİR TANE VERİ APİDEN DEĞİŞTİĞSE O SATIRI SİL VE YENİ EKLE DB YE
                            //AYNI ŞEHİRDEN 2 TANE OLMASINI İSTEMİYORUM         YA 1 YA 0
                            Log.e("OSMAN", "KARŞILATIRMADA HEPSİ EŞİT ÇIKMADI")
                            dbWeather.deleteSelectedCity(sehir) // ÖNCE ŞEHİR VERİLERİ SİLİNDİ
                            dbWeather.insertDataWH( // DB YE YENİ VERİLER EKLENDİ
                                WeatherTablo(
                                    city = sehir,
                                    temp = sicaklik.toString(),
                                    description = aciklama.toString(),
                                    date = tarihYazdir2(),
                                    icon = icon.toString()
                                )
                            )
                            var yeniSehirSicaklik = dbWeather.findSelectedCityTemp(sehir)
                            var yeniSehirAciklama = dbWeather.findSelectedCityDescription(sehir)
                            var yeniSehirIcon = dbWeather.findSelectedCityIcon(sehir)
                            tvSicaklik.text = yeniSehirSicaklik
                            tvAciklama.text = yeniSehirAciklama
                            tvTarih.text = tarihYazdir()
                            geceGunduzIcon(yeniSehirIcon)
                            Log.e("OSMAN", "YENİ VERİLER DBYE YAZILDI ARDINDAN EKRANA BASILDI")
                        }
                    } else if (kacTaneSehir == 0) {  // 0 TANE İSE YAPILICAKLAR
                        //tabloya eklenecek
                        Log.e("OSMAN", "SEÇİLEN ŞEHİR DB DE HİÇ ÇIKMADI")
                        dbWeather.insertDataWH(
                            WeatherTablo(
                                city = sehir,
                                temp = sicaklik.toString(),
                                description = aciklama.toString(),
                                date = tarihYazdir2(),
                                icon = icon.toString()
                            )
                        )//tabloya eklenecek
                        var yeniSehirSicaklik2 = dbWeather.findSelectedCityTemp(sehir)
                        var yeniSehirAciklama2 = dbWeather.findSelectedCityDescription(sehir)
                        var yeniSehirIcon2 = dbWeather.findSelectedCityIcon(sehir)

                        tvSicaklik.text = yeniSehirSicaklik2
                        tvAciklama.text = yeniSehirAciklama2
                        tvTarih.text = tarihYazdir()
                        geceGunduzIcon(yeniSehirIcon2)
                        Log.e("OSMAN", "YENİ VERİLER DBYE YAZILDI ARDINDAN EKRANA BASILDI")
                    } else {  // SEHİRDEN DB DE 2 VEYA DAHA FAZLA OLMASI DURUMU !!! İSTENMEYEN DURUM
                        Log.e("OSMAN", "SEHİR DBYE 2 >= KAYDEDİLMİŞ")
                        dbWeather.deleteSelectedCity(sehir)
                        dbWeather.insertDataWH( // DB YE YENİ VERİLER EKLENDİ
                            WeatherTablo(
                                city = sehir,
                                temp = sicaklik.toString(),
                                description = aciklama.toString(),
                                date = tarihYazdir2(),
                                icon = icon.toString()
                            )
                        )
                        var yeniSehirSicaklik3 = dbWeather.findSelectedCityTemp(sehir)
                        var yeniSehirAciklama3 = dbWeather.findSelectedCityDescription(sehir)
                        var yeniSehirIcon3 = dbWeather.findSelectedCityIcon(sehir)
                        tvSicaklik.text = yeniSehirSicaklik3
                        tvAciklama.text = yeniSehirAciklama3
                        tvTarih.text = tarihYazdir()
                        geceGunduzIcon(yeniSehirIcon3)
                        Log.e("OSMAN", "YENİ VERİLER DBYE YAZILDI ARDINDAN EKRANA BASILDI")
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {}
            })
        MySingleton.getInstance(this).addToRequestQueue(havaDurumuObje)
    }

    // 01 OCAK 2020 F O R M A T I N D A     E K R A N A     B A S I L A N    T A R İ H   F O N K İ S Y O N U
    fun tarihYazdir(): String {
        var currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("tr"))
        val formattedCurrentTime = currentTime.format(formatter)
        return formattedCurrentTime
    }

    // 2020-01-01 F O R M A T I N D A     D B ' Y E     Y A Z I L A N    T A R İ H   F O N K İ S Y O N U
    fun tarihYazdir2(): String {
        var currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("tr"))
        val formattedCurrentTime = currentTime.format(formatter)
        return formattedCurrentTime
    }

    // APİDEN GELEN "2020-01-29T07:00:00+03:00" FORMATINDAKİ TARİHTEN GÜN ADINA ÇEVİREN FONKSİYON
    fun tarihYazdirGunAdi(jsonTarih: String): String {
        var gelenZaman: String = jsonTarih
        var result: ZonedDateTime = ZonedDateTime.parse(gelenZaman, DateTimeFormatter.ISO_DATE_TIME)
        val formatter = DateTimeFormatter.ofPattern("EEEE ", Locale("tr"))
        var formattedTime = result.format(formatter)
        return formattedTime
    }

    // APİDEN GELEN "2020-01-29T07:00:00+03:00" FORMATINDAKİ TARİHİ '01 AYADI'  OLARAK DÖNDÜREN FONKSİYON
    fun tarihYazdirAyGun(jsonTarih: String): String {
        var gelenZaman: String = jsonTarih
        var result: ZonedDateTime = ZonedDateTime.parse(gelenZaman, DateTimeFormatter.ISO_DATE_TIME)
        val formatter = DateTimeFormatter.ofPattern("dd MMMM", Locale("tr"))
        var formattedTime = result.format(formatter)
        return formattedTime
    }

    // gece gunduz icon img yerleştir işi kısaltmak için fonksiyona aldım
    // belki daha sonra tekrar tekrar kullanırım kalabalık yapmasın
    // animasyon işlemleride gece gunduz icon içinde yapıldı
    fun geceGunduzIcon(iconInner: String) {
        if (iconInner?.last() == 'd') {    // G Ü N D Ü Z  'd' apiden gelen gündüz verisi
            tvSehir?.setTextColor(resources.getColor(R.color.snowBir))
            collapsingToolbar.background = getDrawable(R.color.colorPrimary3)
            tvAciklama.setTextColor(resources.getColor(R.color.snowBir))
            tvSicaklik.setTextColor(resources.getColor(R.color.snowBir))
            tvTarih.setTextColor(resources.getColor(R.color.snowBir))
            tvSantigrad.setTextColor(resources.getColor(R.color.snowBir))
            spnSehirler.getBackground().setColorFilter( // spinnerin arrow unun colorunu değiştirme işlemi
                getResources().getColor(R.color.snowBir),
                PorterDuff.Mode.SRC_ATOP
            )
        } else {  //  G E C E  'n' degeri dönüyor Night
            tvSehir?.setTextColor(resources.getColor(R.color.snowBir))
            collapsingToolbar.background = getDrawable(R.drawable.geceplanikes)//  gece arka planı geliyor yıldızlı
            tvAciklama.setTextColor(resources.getColor(R.color.snowBir))
            tvSicaklik.setTextColor(resources.getColor(R.color.snowBir))
            tvTarih.setTextColor(resources.getColor(R.color.snowBir))
            tvSantigrad.setTextColor(resources.getColor(R.color.snowBir))
            spnSehirler.getBackground().setColorFilter(// arrow renk değişimi işlemi
                getResources().getColor(R.color.snowBir),
                PorterDuff.Mode.SRC_ATOP
            )
        }
        var resimDosyaAdi = resources.getIdentifier(
            "icon_" + iconInner?.sonKarakteriSil(),
            "drawable",
            packageName
        )
        //imgHavaDurumu.setImageResource(resimDosyaAdi)

        // anim dosya adi nı apiden gelen degerleri bir raw değeri gibi gösterip kullandığımız kısım
        //resources a bir id atama işlemi
        var animDosyaAdi =
            resources.getIdentifier("anim" + iconInner?.sonKarakteriSil(), "raw", packageName)

        // animasyon 4 parçalı bulut animasyonu AMA lottie de böyle bir animasyon yok bende kapanan bulut animasyonunu
        // tersten oynatarak sanki parçalı bulut efekti verdim bunun için bir sürü karar yapısı yazmam gerekti
        if (animDosyaAdi == R.raw.anim04) {
            tersEdildiMi = true  // animasyon reverse edildiğinde true
            animationWeather.setAnimation(animDosyaAdi)
            animationWeather.visibility = View.VISIBLE
            animationWeather.reverseAnimationSpeed()// tersten oynatma animasyonu (reverse)
            animationWeather.loop(true)
            animationWeather.playAnimation()
            // bir kere ters edildikten sonra başka şehir tıklandığında animasyonlarda hata çıkmaması için yağtığım kontrol
        } else if ((animDosyaAdi != R.raw.anim04) && (tersEdildiMi == true)) {
            // gece olan yerlerde gece arka planı geliyor
            // gece olduğu halde acık hava durumuna günes ikonu geldiği için bende ay animasyonlarını bu durumlarda getirtiyorum
            // karar yapıları bu şekilde
            if ((animDosyaAdi == R.raw.anim01) && (iconInner?.last() == 'n')) {
                // animasyon 1 (gunes) ve api degeri night ise
                animationWeather.setAnimation(R.raw.geceayveyildizlar)
                animationWeather.reverseAnimationSpeed()
                animationWeather.visibility = View.VISIBLE
                animationWeather.loop(true)
                animationWeather.playAnimation()
                tersEdildiMi = false
            } else if ((animDosyaAdi == R.raw.anim02) && (iconInner?.last() == 'n')) {
                // animasyon 2 (gunesliBirAnim) ve api degeri night ise
                animationWeather.setAnimation(R.raw.moon02)
                animationWeather.reverseAnimationSpeed()
                animationWeather.visibility = View.VISIBLE
                animationWeather.loop(true)
                animationWeather.playAnimation()
                tersEdildiMi = false
            } else {
                // olmaması durumu
                animationWeather.setAnimation(animDosyaAdi)
                animationWeather.reverseAnimationSpeed()
                animationWeather.visibility = View.VISIBLE
                animationWeather.loop(true)
                animationWeather.playAnimation()
                tersEdildiMi = false
            }
        } else {
            if ((animDosyaAdi == R.raw.anim01) && (iconInner?.last() == 'n')) {
                animationWeather.setAnimation(R.raw.geceayveyildizlar)
                animationWeather.visibility = View.VISIBLE
                animationWeather.loop(true)
                animationWeather.playAnimation()
                tersEdildiMi = false
            }else if ((animDosyaAdi == R.raw.anim02) && (iconInner?.last() == 'n')) {
                animationWeather.setAnimation(R.raw.moon02)
                animationWeather.visibility = View.VISIBLE
                animationWeather.loop(true)
                animationWeather.playAnimation()
                tersEdildiMi = false
            } else {
                animationWeather.setAnimation(animDosyaAdi)
                animationWeather.visibility = View.VISIBLE
                animationWeather.loop(true)
                animationWeather.playAnimation()
                tersEdildiMi = false
            }
        }
    }

    // 5 günlük h-d raporu için accu weather ın apisini kullanıyorum. 2 tane sorgu yapmam lazım
    // 1.sorgu location keyi alabilmek için || 2.sorgu aldığım location key ile 5gunluk h-d raporunu alabilmek için
    // iç içe yazmamın sebebi onResponse nin içinde return kullanılmamasından dolayı.
    fun fetchingLocationKey(sehirAdiLocKey: String) {

        listHavaArray.clear()   // hava listesini temizleyip yeni listeyi en aşağıda yazdırıyorum
        val locationKeyUrl =    // free request count dolduğu yeni api key aldım
            "https://dataservice.accuweather.com/locations/v1/cities/search?apikey=tME4hGctpv11dTd92pVKmFxiXIEtVKmk&q=" + sehirAdiLocKey + "&language=tr&details=false"
       // val locationKeyUrl =
            //"https://dataservice.accuweather.com/locations/v1/cities/search?apikey=z547Q7RZHRcI3FITkEgqwQLdBoyADvLb&q=" + sehirAdiLocKey + "&language=tr&details=false"
        var request = JsonArrayRequest(locationKeyUrl, object : Response.Listener<JSONArray> {  //1 . S O R G U
            override fun onResponse(response: JSONArray?) {
                var jsonobjectDeneme = response?.getJSONObject(0)
                locationKey = jsonobjectDeneme!!.getString("Key")
                var keyliUrl =
                    "https://dataservice.accuweather.com/forecasts/v1/daily/5day/" + locationKey + "?apikey=tME4hGctpv11dTd92pVKmFxiXIEtVKmk&language=tr&details=false&metric=true"
                //var keyliUrl =
                   // "https://dataservice.accuweather.com/forecasts/v1/daily/5day/" + locationKey + "?apikey=z547Q7RZHRcI3FITkEgqwQLdBoyADvLb&language=tr&details=false&metric=true"
                var request2 = JsonObjectRequest(   // 2 . S O R G U
                    Request.Method.GET,
                    keyliUrl,
                    null,
                    object : Response.Listener<JSONObject> {
                        override fun onResponse(response: JSONObject?) {
                            for (i in 0..4) {  // 5 TANE OBJEDE DÖNÜP DEGERLERİ ARRAYLİSTE ATAN FOR DÖNGÜSÜ
                                var dailyForecast = response?.getJSONArray("DailyForecasts")
                                Log.e("OSMAN", "response uzunluğu for içi: " + response!!.length())
                                try {
                                    var dateDeneme =
                                        dailyForecast?.getJSONObject(i)?.getString("Date")
                                    var dayName = tarihYazdirGunAdi(dateDeneme!!)
                                    var mounthNumberDay = tarihYazdirAyGun(dateDeneme!!)
                                    var minimumTemp =
                                        dailyForecast?.getJSONObject(i)
                                            ?.getJSONObject("Temperature")
                                            ?.getJSONObject("Minimum")?.getDouble("Value")
                                    var maximumTemp =
                                        dailyForecast?.getJSONObject(i)
                                            ?.getJSONObject("Temperature")
                                            ?.getJSONObject("Maximum")?.getDouble("Value")
                                    var accuIcon =
                                        dailyForecast?.getJSONObject(i)?.getJSONObject("Day")
                                            ?.getInt("Icon")
                                    // APİDEN GELEN İCON BİLGİSİNE BİR ID ATAMASI YAPTIPIM KISIM
                                    var iconNameFormatter = resources.getIdentifier(
                                        "aicon" + accuIcon,
                                        "drawable",
                                        packageName
                                    )
                                    var besGunHavaDeger = besGunHava()
                                    besGunHavaDeger.gunAdi = dayName
                                    besGunHavaDeger.gunAyNumber = mounthNumberDay
                                    besGunHavaDeger.minimumTemp = minimumTemp!!.toInt()
                                    besGunHavaDeger.maximumTemp = maximumTemp!!.toInt()
                                    besGunHavaDeger.accuIcon = iconNameFormatter
                                    listHavaArray.add(besGunHavaDeger)
                                } catch (e: JSONException) {
                                    Log.e("OSMAN", "try catch hata vardi : " + e.printStackTrace())
                                }
                                setupRecyclerView(listHavaArray)    // R E C Y C L E R W İ E W    A T A M A
                            }
                        }
                    },
                    object : Response.ErrorListener {
                        override fun onErrorResponse(error: VolleyError?) {}
                    })//2.SORGU BİTİŞ
                MySingleton.getInstance(this@MainActivity).addToRequestQueue(request2)
            }
        }, object : Response.ErrorListener {
            override fun onErrorResponse(error: VolleyError?) {
                Log.e("OSMAN", "Errorra girdi" + error)
            }
        })// 1.SORGU BİTİŞ
        MySingleton.getInstance(this).addToRequestQueue(request)
    }

    // kullanıcının o an bulunduğu şehrin location keyini alıp sorguya sokan ve recyclerviewa atama yapan
    // fonksiyon . üstekinden tek varkı bu
    fun fetchingLocationKeyOankiSehir(lat: String, longt: String) {
        listHavaArray.clear()

        val locationKeyOan =
            "https://dataservice.accuweather.com/locations/v1/cities/geoposition/search?apikey=tME4hGctpv11dTd92pVKmFxiXIEtVKmk&q=" + lat + "%2C" + longt + "&language=tr&details=false&toplevel=false"

        //val locationKeyOan =
            //"https://dataservice.accuweather.com/locations/v1/cities/geoposition/search?apikey=z547Q7RZHRcI3FITkEgqwQLdBoyADvLb&q=" + lat + "%2C" + longt + "&language=tr&details=false&toplevel=false"
        var requestOan = JsonObjectRequest(
            Request.Method.GET,
            locationKeyOan,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {
                    var key = response?.getString("Key")
                    localizedName = response?.getString("LocalizedName")!!
                    collapsingToolbar.title = localizedName
                    collapsingToolbar.setCollapsedTitleTextColor(resources.getColor(R.color.snowBir))
                    Log.e("OSMAN", "O an ki key ? = " + key)
                    Log.e("OSMAN", "O an ki localname ? = " + localizedName)
                    var keyliUrl =
                        "https://dataservice.accuweather.com/forecasts/v1/daily/5day/" + key + "?apikey=tME4hGctpv11dTd92pVKmFxiXIEtVKmk&language=tr&details=false&metric=true"
                    //var keyliUrl =
                        //"https://dataservice.accuweather.com/forecasts/v1/daily/5day/" + key + "?apikey=z547Q7RZHRcI3FITkEgqwQLdBoyADvLb&language=tr&details=false&metric=true"
                    var request2 = JsonObjectRequest(
                        Request.Method.GET,
                        keyliUrl,
                        null,
                        object : Response.Listener<JSONObject> {
                            override fun onResponse(response: JSONObject?) {
                                for (i in 0..4) {
                                    var dailyForecast = response?.getJSONArray("DailyForecasts")
                                    Log.e(
                                        "OSMAN",
                                        "response uzunluğu for içi: " + response!!.length()
                                    )
                                    try {
                                        var dateDeneme =
                                            dailyForecast?.getJSONObject(i)?.getString("Date")
                                        var dayName = tarihYazdirGunAdi(dateDeneme!!)
                                        var mounthNumberDay = tarihYazdirAyGun(dateDeneme!!)
                                        var minimumTemp =
                                            dailyForecast?.getJSONObject(i)
                                                ?.getJSONObject("Temperature")
                                                ?.getJSONObject("Minimum")?.getDouble("Value")
                                        var maximumTemp =
                                            dailyForecast?.getJSONObject(i)
                                                ?.getJSONObject("Temperature")
                                                ?.getJSONObject("Maximum")?.getDouble("Value")
                                        var accuIcon =
                                            dailyForecast?.getJSONObject(i)?.getJSONObject("Day")
                                                ?.getInt("Icon")

                                        var iconNameFormatter = resources.getIdentifier(
                                            "aicon" + accuIcon,
                                            "drawable",
                                            packageName
                                        )

                                        var besGunHavaDeger = besGunHava()
                                        besGunHavaDeger.gunAdi = dayName
                                        besGunHavaDeger.gunAyNumber = mounthNumberDay
                                        besGunHavaDeger.minimumTemp = minimumTemp!!.toInt()
                                        besGunHavaDeger.maximumTemp = maximumTemp!!.toInt()
                                        besGunHavaDeger.accuIcon = iconNameFormatter
                                        listHavaArray.add(besGunHavaDeger)

                                    } catch (e: JSONException) {
                                        Log.e(
                                            "OSMAN",
                                            "try catch hata vardi : " + e.printStackTrace()
                                        )
                                    }
                                    setupRecyclerView(listHavaArray)
                                }
                            }
                        },
                        object : Response.ErrorListener {
                            override fun onErrorResponse(error: VolleyError?) {}
                        })
                    MySingleton.getInstance(this@MainActivity).addToRequestQueue(request2)
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    Log.e("OSMAN", "Error Listener ", error)
                }
            })
        MySingleton.getInstance(this).addToRequestQueue(requestOan)
    }

    // R E C Y C L E R V İ E W     K U R U L U M
    fun setupRecyclerView(listHava: ArrayList<besGunHava>?) {
        var myAdapter = RecyclerViewAdapter(this, listHava!!)
        recyclerViewBes.layoutManager = LinearLayoutManager(this)
        recyclerViewBes.adapter = myAdapter
    }

    //GERİ BUTONUNA BASILDIĞINDA ANİMASYON OLMASI İÇİN ON BACK PRESSED OVERRİDE EDİLDİ
    override fun onBackPressed() {
        super.onBackPressed()
        customType(this@MainActivity, "right-to-left")// İNTENT ANİMATİON
    }

    // KULLANICININ İNTERNETE BAĞLI OLUP OLMADIĞINI KONTROL ETTİRDİĞİM KISIM
    object MyReachability {
        private val REACHABILITY_SERVER = "https://www.google.com.tr" // istenilen bir url girilebilir (bazen bağlı olmasına rağmen ping atamıyor)
        private fun hasNetworkAvailable(context: Context): Boolean {
            val service = Context.CONNECTIVITY_SERVICE
            val manager = context.getSystemService(service) as ConnectivityManager?
            val network = manager?.activeNetworkInfo
            Log.e("OSMAN", "hasNetworkAvailable: ${(network != null)}")
            return (network != null)
        }
        fun hasInternetConnected(context: Context): Boolean {
            if (hasNetworkAvailable(context)) {
                try {
                    val connection = URL(REACHABILITY_SERVER).openConnection() as HttpURLConnection
                    connection.setRequestProperty("User-Agent", "Test")
                    connection.setRequestProperty("Connection", "close")
                    connection.connectTimeout = 45000
                    connection.connect()
                    Log.e("OSMAN", "hasInternetConnected: ${(connection.responseCode == 200)}")
                    return (connection.responseCode == 200)
                } catch (e: IOException) {
                    Log.e("OSMAN", "Error checking internet connection", e)
                }
            } else {
                Log.w("OSMAN", "No network available!")
            }
            Log.d("OSMAN", "hasInternetConnected: false")
            return false
        }
    }
}

// APİDEN GELEN D VEYA N VERİSİNİ SİLMEK İÇİN YAZDIPIM FUN
private fun String?.sonKarakteriSil(): String {
    // 50n olan ifadeyi 50 olarak geri döndürür.
    return this!!.substring(0, this!!.length - 1)
}
