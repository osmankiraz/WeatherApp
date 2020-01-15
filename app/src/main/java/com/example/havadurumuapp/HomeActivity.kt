package com.example.havadurumuapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {


    var uptodowninfinite: Animation?=null
    var doksanderecedonme: Animation?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        uptodowninfinite= AnimationUtils.loadAnimation(applicationContext,R.anim.downtoupinfinity)
        doksanderecedonme= AnimationUtils.loadAnimation(applicationContext,R.anim.doksanrotate)
        imgSemsiye.animation=uptodowninfinite
        imgExchange.animation=doksanderecedonme


        tvWeather.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent=Intent(this@HomeActivity,MainActivity::class.java)
                startActivity(intent)
            }

        })

        tvCurrency.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent=Intent(this@HomeActivity,CurrencyActivity::class.java)
                startActivity(intent)

            }
        })

    }
}
