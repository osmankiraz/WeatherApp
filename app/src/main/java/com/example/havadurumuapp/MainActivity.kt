package com.example.havadurumuapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    var tvSehir: TextView? = null
    var location: SimpleLocation? = null
    var latitude: String? = null
    var longitude: String? = null
    val dbWeather by lazy { DBWeatherHelper(this) }

    override fun onNothingSelected(p0: AdapterView<*>?) {


    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        tvSehir = view as TextView

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
                    oAnkiSehriGetir(latitude, longitude)
                }
            }


        } else {
            // SPİNNERDAN SECİLEN SEHRİN VERİLERİNİN BASILDIĞI KISIM
            var secilenSehir = parent?.getItemAtPosition(position).toString()
            tvSehir = view as TextView
            verileriGetir(secilenSehir)
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        spnSehirler.setPositiveButton("KAPAT")
        spnSehirler.onItemSelectedListener =
            this // SPİNNERDAN BİŞEY SEÇİLDİĞİNDE BİR HAREKET OLMASI İÇİN BUNU YAZMAK ZORUNDAYIZ

        spnSehirler.setSelection(1)

        //verileriGetir("istanbul")


    }

    private fun oAnkiSehriGetir(lat: String?, longt: String?) {

        var sehirAdi: String? = null

        val sehirUrl =
            "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + longt + "&appid=b10a542e4c2b6323b9da3b7910cd2d0d&lang=tr&units=metric"
        //val ankaraUrl ="https://api.openweathermap.org/data/2.5/weather?lat=35.65&lon=139.83&appid=b10a542e4c2b6323b9da3b7910cd2d0d&lang=tr&units=metric"
        val havaDurumuObje2 = JsonObjectRequest(
            Request.Method.GET,
            sehirUrl,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {
                    var main = response?.getJSONObject("main")
                    var sicaklik = main?.getInt("temp")
                    //tvSicaklik.text = sicaklik.toString() ////////////////////////////////////////////////

                    sehirAdi = response?.getString("name")
                    //tvSehir?.setText(sehirAdi)//////////////////////////////////////////
                    Log.e("OSMAN", "sehir adi : " + sehirAdi)
                    Log.e("OSMAN", "lat : " + lat)
                    Log.e("OSMAN", "long : " + longt)

                    var weather = response?.getJSONArray("weather")
                    var aciklama = weather?.getJSONObject(0)?.getString("description")
                    //tvAciklama.text = aciklama  ///////////////////////////////////////
                    var icon = weather?.getJSONObject(0)?.getString("icon")

                    //geceGunduzIcon(icon.toString()) /////////////////////////////

                    var sehirVarmi: Boolean
                    sehirVarmi = dbWeather.isEmptyTable()
                    Log.e("OSMAN", "TABLO BOŞ İSE TRU GELECEK? ? = " + sehirVarmi)
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                    var kacTaneSehir = 0
                    kacTaneSehir = dbWeather.kacTane(sehirAdi!!)
                    Log.e("OSMAN", sehirAdi+" ŞEHRİNDEN  DB'DE KAÇ TANE VAR ? = " + kacTaneSehir)

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

                            tvSehir?.setText(sehirAdi)
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
                            var yeniSehirAdi=dbWeather.findSelectedCity(sehirAdi.toString())
                            var yeniSehirSicaklik=dbWeather.findSelectedCityTemp(sehirAdi.toString())
                            var yeniSehirAciklama=dbWeather.findSelectedCityDescription(sehirAdi.toString())
                            var yeniSehirIcon=dbWeather.findSelectedCityIcon(sehirAdi.toString())

                            tvSehir?.setText(yeniSehirAdi)
                            tvSicaklik.text=yeniSehirSicaklik
                            tvAciklama.text=yeniSehirAciklama
                            tvTarih.text=tarihYazdir()
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

                        var yeniSehirAdi2=dbWeather.findSelectedCity(sehirAdi.toString())
                        var yeniSehirSicaklik2=dbWeather.findSelectedCityTemp(sehirAdi.toString())
                        var yeniSehirAciklama2=dbWeather.findSelectedCityDescription(sehirAdi.toString())
                        var yeniSehirIcon2=dbWeather.findSelectedCityIcon(sehirAdi.toString())

                        tvSehir?.setText(yeniSehirAdi2)
                        tvSicaklik.text=yeniSehirSicaklik2
                        tvAciklama.text=yeniSehirAciklama2
                        tvTarih.text=tarihYazdir()
                        geceGunduzIcon(yeniSehirIcon2)
                        Log.e("OSMAN", "YENİ VERİLER DBYE YAZILDI ARDINDAN EKRANA BASILDI")


                    }else{  // SEHİRDEN DB DE 2 VEYA DAHA FAZLA OLMASI DURUMU !!! İSTENMEYEN DURUM
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
                        var yeniSehirAdi3=dbWeather.findSelectedCity(sehirAdi.toString())
                        var yeniSehirSicaklik3=dbWeather.findSelectedCityTemp(sehirAdi.toString())
                        var yeniSehirAciklama3=dbWeather.findSelectedCityDescription(sehirAdi.toString())
                        var yeniSehirIcon3=dbWeather.findSelectedCityIcon(sehirAdi.toString())

                        tvSehir?.setText(yeniSehirAdi3)
                        tvSicaklik.text=yeniSehirSicaklik3
                        tvAciklama.text=yeniSehirAciklama3
                        tvTarih.text=tarihYazdir()
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
        if (requestCode == 60) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                location = SimpleLocation(this)
                latitude = String.format("%.2f", location?.latitude)
                longitude = String.format("%.2f", location?.longitude)
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

        val ankaraUrl =
            "https://api.openweathermap.org/data/2.5/weather?q=" + sehir + "&appid=b10a542e4c2b6323b9da3b7910cd2d0d&lang=tr&units=metric"
        val havaDurumuObje = JsonObjectRequest(
            Request.Method.GET,
            ankaraUrl,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {
                    var main = response?.getJSONObject("main")
                    var sicaklik = main?.getInt("temp")
                    var sehirAdi = response?.getString("name")
                    //tvSehir.text=sehirAdi
                    var weather = response?.getJSONArray("weather")
                    var aciklama = weather?.getJSONObject(0)?.getString("description")
                    var icon = weather?.getJSONObject(0)?.getString("icon")

                    //dbWeather.insertDataWH(WeatherTablo(city=sehir,temp =sicaklik.toString(),description = aciklama.toString(),date ="14-01-2020",icon = "icon_09"))
                    //dbWeather.insertDataWH(WeatherTablo(city="Ankara",temp ="36",description = "insan",date ="14-01-2020",icon = "icon_10"))


                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    //dbWeather.deleteAllData()
                    var sehirVarmi: Boolean
                    sehirVarmi = dbWeather.isEmptyTable()
                    Log.e("OSMAN", "TABLO BOŞ İSE TRU GELECEK? ? = " + sehirVarmi)
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                    var kacTaneSehir = 0
                    kacTaneSehir = dbWeather.kacTane(sehir)
                    Log.e("OSMAN", sehir+" ŞEHRİNDEN  DB'DE KAÇ TANE VAR ? = " + kacTaneSehir)

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
                                    city = sehirAdi.toString(),
                                    temp = sicaklik.toString(),
                                    description = aciklama.toString(),
                                    date = tarihYazdir2(),
                                    icon = icon.toString()
                                )
                            )

                            var yeniSehirSicaklik=dbWeather.findSelectedCityTemp(sehir)
                            var yeniSehirAciklama=dbWeather.findSelectedCityDescription(sehir)
                            var yeniSehirIcon=dbWeather.findSelectedCityIcon(sehir)

                            tvSicaklik.text=yeniSehirSicaklik
                            tvAciklama.text=yeniSehirAciklama
                            tvTarih.text=tarihYazdir()
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

                        var yeniSehirSicaklik2=dbWeather.findSelectedCityTemp(sehir)
                        var yeniSehirAciklama2=dbWeather.findSelectedCityDescription(sehir)
                        var yeniSehirIcon2=dbWeather.findSelectedCityIcon(sehir)

                        tvSicaklik.text=yeniSehirSicaklik2
                        tvAciklama.text=yeniSehirAciklama2
                        tvTarih.text=tarihYazdir()
                        geceGunduzIcon(yeniSehirIcon2)
                        Log.e("OSMAN", "YENİ VERİLER DBYE YAZILDI ARDINDAN EKRANA BASILDI")


                    }else{  // SEHİRDEN DB DE 2 VEYA DAHA FAZLA OLMASI DURUMU !!! İSTENMEYEN DURUM
                        Log.e("OSMAN", "SEHİR DBYE 2 >= KAYDEDİLMİŞ")
                        dbWeather.deleteSelectedCity(sehir)
                        dbWeather.insertDataWH( // DB YE YENİ VERİLER EKLENDİ
                            WeatherTablo(
                                city = sehirAdi.toString(),
                                temp = sicaklik.toString(),
                                description = aciklama.toString(),
                                date = tarihYazdir2(),
                                icon = icon.toString()
                            )
                        )

                        var yeniSehirSicaklik3=dbWeather.findSelectedCityTemp(sehir)
                        var yeniSehirAciklama3=dbWeather.findSelectedCityDescription(sehir)
                        var yeniSehirIcon3=dbWeather.findSelectedCityIcon(sehir)

                        tvSicaklik.text=yeniSehirSicaklik3
                        tvAciklama.text=yeniSehirAciklama3
                        tvTarih.text=tarihYazdir()
                        geceGunduzIcon(yeniSehirIcon3)
                        Log.e("OSMAN", "YENİ VERİLER DBYE YAZILDI ARDINDAN EKRANA BASILDI")

                    }


                }

            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {

                }

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

    // gece gunduz icon img yerleştir işi kısaltmak için fonksiyona aldım
    // belki daha sonra tekrar tekrar kullanırım kalabalık yapmasın
    fun geceGunduzIcon(iconInner: String) {
        if (iconInner?.last() == 'd') {    // G Ü N D Ü Z

            tvSehir?.setTextColor(resources.getColor(R.color.colorPrimaryDark))
            rootLayout.background = getDrawable(R.drawable.dagvar)
            tvAciklama.setTextColor(resources.getColor(R.color.colorPrimaryDark))
            tvSicaklik.setTextColor(resources.getColor(R.color.colorPrimaryDark))
            tvTarih.setTextColor(resources.getColor(R.color.colorPrimaryDark))
            tvSantigrad.setTextColor(resources.getColor(R.color.colorPrimaryDark))
            spnSehirler.getBackground().setColorFilter(
                getResources().getColor(R.color.colorPrimaryDark),
                PorterDuff.Mode.SRC_ATOP
            )

        } else {  //  G E C E
            tvSehir?.setTextColor(resources.getColor(R.color.snowBir))
            rootLayout.background = getDrawable(R.drawable.gecearkaplan)
            tvAciklama.setTextColor(resources.getColor(R.color.snowBir))
            tvSicaklik.setTextColor(resources.getColor(R.color.snowBir))
            tvTarih.setTextColor(resources.getColor(R.color.snowBir))
            tvSantigrad.setTextColor(resources.getColor(R.color.snowBir))
            spnSehirler.getBackground().setColorFilter(
                getResources().getColor(R.color.snowBir),
                PorterDuff.Mode.SRC_ATOP
            )

        }

        var resimDosyaAdi = resources.getIdentifier(
            "icon_" + iconInner?.sonKarakteriSil(),
            "drawable",
            packageName
        )
        imgHavaDurumu.setImageResource(resimDosyaAdi)



        var animDosyaAdi=resources.getIdentifier("anim"+iconInner?.sonKarakteriSil(),"raw",packageName)


        if(animDosyaAdi == R.raw.anim04 ){
            animationWeather.setAnimation(animDosyaAdi)
            animationWeather.visibility=View.VISIBLE
            animationWeather.reverseAnimationSpeed()
            animationWeather.loop(true)
            animationWeather.playAnimation()
        }else{
            animationWeather.setAnimation(animDosyaAdi)
            animationWeather.visibility=View.VISIBLE
            animationWeather.loop(true)
            animationWeather.playAnimation()
        }





    }

}

private fun String?.sonKarakteriSil(): String {
    // 50n olan ifadeyi 50 olarak geri döndürür.
    return this!!.substring(0, this!!.length - 1)

}
