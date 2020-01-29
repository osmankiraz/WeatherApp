package com.example.havadurumuapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.havadurumuapp.Model.besGunHava
import com.example.havadurumuapp.R

//holdera ardından listeye ardından recyclerviewa atama yap
class RecyclerViewAdapter(var context: Context, var mdata: ArrayList<besGunHava>) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // tek satır layouttaki değerleri kullanabilmek içni inflate ettim.
        var view: View
        var inflater: LayoutInflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.tek_satir_bes_gunluk_hava, parent, false)
        return MyViewHolder(view)
    }
    // listenin uzunluğunu döndüren fonksiyon
    override fun getItemCount(): Int {return mdata.size}

    // tektek birbirine atama işlemi
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tv_dayName.setText(mdata.get(position).gunAdi)
        holder.tv_dayDate.setText(mdata.get(position).gunAyNumber)
        holder.tv_maxTemp.setText(mdata.get(position).maximumTemp.toString())
        holder.tv_minTemp.setText(mdata.get(position).minimumTemp.toString())
        holder.img_accuicon.setImageResource(mdata.get(position).accuIcon!!) // hata olabilir ! ! ! !
    }

    //modeldeki veriler ile inflate edilen leri birbirlerine atama
    class MyViewHolder(view:View):RecyclerView.ViewHolder(view) {
        var tv_dayName: TextView =view.findViewById(R.id.txtGunTarih)
        var tv_dayDate:TextView=view.findViewById(R.id.txtSayiTarih)
        var tv_maxTemp:TextView=view.findViewById(R.id.txtBesGunduzSicaklik)
        var tv_minTemp:TextView=view.findViewById(R.id.txtBesGeceSicaklik)
        var img_accuicon:ImageView=view.findViewById(R.id.imgBesgunIcon)    // ! ! ! !

    }

}