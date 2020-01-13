package com.example.havadurumuapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.location.Location
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
import com.android.volley.toolbox.StringRequest
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.spinner_tek_satir.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() , AdapterView.OnItemSelectedListener {


    var tvSehir: TextView?=null
    var location:SimpleLocation?=null
    var latitude:String?=null
    var longitude:String?=null
    override fun onNothingSelected(p0: AdapterView<*>?) {


    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        tvSehir=view as TextView

        if(position==0){

            location= SimpleLocation(this)
            if(!location!!.hasLocationEnabled()){
                spnSehirler.setSelection(1)
                Toast.makeText(this,"GPS AÇ",Toast.LENGTH_LONG).show()
                SimpleLocation.openSettings(this)
            }else{
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED  ){
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),60)
                }else{
                    location= SimpleLocation(this)
                    latitude=String.format("%.2f",location?.latitude)
                    longitude=String.format("%.2f",location?.longitude)
                    oAnkiSehriGetir(latitude,longitude)
                }
            }


        }else{
            // SPİNNERDAN SECİLEN SEHRİN VERİLERİNİN BASILDIĞI KISIM
            var secilenSehir=parent?.getItemAtPosition(position).toString()
            tvSehir=view as TextView
            verileriGetir(secilenSehir)
        }



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var spinnerAdapter= ArrayAdapter.createFromResource(this,R.array.sehirler,R.layout.spinner_tek_satir)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // ARROW COLOR CHANGE
        spnSehirler.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)

        spnSehirler.adapter=spinnerAdapter
        spnSehirler.setTitle("Şehir Seçiniz")
        spnSehirler.setPositiveButton("KAPAT")
        spnSehirler.onItemSelectedListener=this // SPİNNERDAN BİŞEY SEÇİLDİĞİNDE BİR HAREKET OLMASI İÇİN BUNU YAZMAK ZORUNDAYIZ

        spnSehirler.setSelection(1)

       //location=SimpleLocation(this)





        verileriGetir("istanbul")


    }

    private fun oAnkiSehriGetir(lat: String?,longt:String?){

        var sehirAdi:String?=null

        val sehirUrl ="https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+longt+"&appid=b10a542e4c2b6323b9da3b7910cd2d0d&lang=tr&units=metric"
        //val ankaraUrl ="https://api.openweathermap.org/data/2.5/weather?lat=35.65&lon=139.83&appid=b10a542e4c2b6323b9da3b7910cd2d0d&lang=tr&units=metric"
        val havaDurumuObje2=JsonObjectRequest(Request.Method.GET,sehirUrl,null, object : Response.Listener<JSONObject>{
            override fun onResponse(response: JSONObject?) {
                var main =response?.getJSONObject("main")


                var sicaklik=main?.getInt("temp")
                tvSicaklik.text=sicaklik.toString()

                sehirAdi=response?.getString("name")
                tvSehir?.setText(sehirAdi)
                Log.e("OSMAN","sehir adi : "+sehirAdi)
                Log.e("OSMAN","lat : "+lat)
                Log.e("OSMAN","long : "+longt)

                var weather =response?.getJSONArray("weather")
                var aciklama=weather?.getJSONObject(0)?.getString("description")
                tvAciklama.text=aciklama
                var icon=weather?.getJSONObject(0)?.getString("icon")

                if(icon?.last() == 'd'){    // G Ü N D Ü Z

                    tvSehir?.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    rootLayout.background=getDrawable(R.drawable.buson)
                    tvAciklama.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvSicaklik.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvTarih.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvSantigrad.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    spnSehirler.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)

                }else{  //  G E C E
                    tvSehir?.setTextColor(resources.getColor(R.color.snowBir))
                    rootLayout.background=getDrawable(R.drawable.gecearkaplan)
                    tvAciklama.setTextColor(resources.getColor(R.color.snowBir))
                    tvSicaklik.setTextColor(resources.getColor(R.color.snowBir))
                    tvTarih.setTextColor(resources.getColor(R.color.snowBir))
                    tvSantigrad.setTextColor(resources.getColor(R.color.snowBir))
                    spnSehirler.getBackground().setColorFilter(getResources().getColor(R.color.snowBir), PorterDuff.Mode.SRC_ATOP)

                }

                var resimDosyaAdi=resources.getIdentifier("icon_"+icon?.sonKarakteriSil(),"drawable",packageName)//R.drawable.icon
                imgHavaDurumu.setImageResource(resimDosyaAdi)

                tvTarih.text=tarihYazdir()


            }

        },object:Response.ErrorListener{
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
        if(requestCode ==60){
            if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                location= SimpleLocation(this)
                latitude=String.format("%.2f",location?.latitude)
                longitude=String.format("%.2f",location?.longitude)
                oAnkiSehriGetir(latitude,longitude)
            }else{
                spnSehirler.setSelection(1)
                Toast.makeText(this,"GPS verisini açmalısın",Toast.LENGTH_LONG).show()
            }


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun verileriGetir(sehir:String){

        val ankaraUrl ="https://api.openweathermap.org/data/2.5/weather?q="+sehir+"&appid=b10a542e4c2b6323b9da3b7910cd2d0d&lang=tr&units=metric"
        val havaDurumuObje=JsonObjectRequest(Request.Method.GET,ankaraUrl,null, object : Response.Listener<JSONObject>{
            override fun onResponse(response: JSONObject?) {
                var main =response?.getJSONObject("main")
                var sicaklik=main?.getInt("temp")
                tvSicaklik.text=sicaklik.toString()

                var sehirAdi=response?.getString("name")
                //tvSehir.text=sehirAdi

                var weather =response?.getJSONArray("weather")
                var aciklama=weather?.getJSONObject(0)?.getString("description")
                tvAciklama.text=aciklama
                var icon=weather?.getJSONObject(0)?.getString("icon")

                if(icon?.last() == 'd'){    // G Ü N D Ü Z

                    tvSehir?.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    rootLayout.background=getDrawable(R.drawable.buson)
                    tvAciklama.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvSicaklik.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvTarih.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    tvSantigrad.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                    spnSehirler.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)

                }else{  //  G E C E
                    tvSehir?.setTextColor(resources.getColor(R.color.snowBir))
                    rootLayout.background=getDrawable(R.drawable.gecearkaplan)
                    tvAciklama.setTextColor(resources.getColor(R.color.snowBir))
                    tvSicaklik.setTextColor(resources.getColor(R.color.snowBir))
                    tvTarih.setTextColor(resources.getColor(R.color.snowBir))
                    tvSantigrad.setTextColor(resources.getColor(R.color.snowBir))
                    spnSehirler.getBackground().setColorFilter(getResources().getColor(R.color.snowBir), PorterDuff.Mode.SRC_ATOP)

                }

                var resimDosyaAdi=resources.getIdentifier("icon_"+icon?.sonKarakteriSil(),"drawable",packageName)//R.drawable.icon
                imgHavaDurumu.setImageResource(resimDosyaAdi)

                tvTarih.text=tarihYazdir()


            }

        },object:Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {

            }

        })


        MySingleton.getInstance(this).addToRequestQueue(havaDurumuObje)

    }

    fun tarihYazdir():String{


        var currentTime=LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("tr"))
        val formattedCurrentTime=currentTime.format(formatter)

        return formattedCurrentTime

    }
}

private fun String?.sonKarakteriSil(): String {
    // 50n olan ifadeyi 50 olarak geri döndürür.
    return this!!.substring(0,this!!.length-1)

}
