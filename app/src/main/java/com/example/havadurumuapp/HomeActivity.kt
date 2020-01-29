package com.example.havadurumuapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_home.*
import maes.tech.intentanim.CustomIntent.customType

class HomeActivity : AppCompatActivity() {

    // A Ç I L I Ş       A N İ M A S Y O N L A R I
    var uptodowninfinite: Animation?=null
    var doksanderecedonme: Animation?=null
    var soldansaga: Animation?=null
    var sagdansola: Animation?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // A N İ M A S Y O N     A T A M A L A R I
        uptodowninfinite= AnimationUtils.loadAnimation(applicationContext,R.anim.downtoupinfinity)
        doksanderecedonme= AnimationUtils.loadAnimation(applicationContext,R.anim.doksanrotate)
        soldansaga= AnimationUtils.loadAnimation(applicationContext,R.anim.lefttoright)
        sagdansola= AnimationUtils.loadAnimation(applicationContext,R.anim.righttoleft)
        imgSemsiye.animation=soldansaga
        tvWeather.animation=soldansaga
        imgExchange.animation=sagdansola
        tvCurrency.animation=sagdansola

        // BUTONA TIKLANDIKTAN SONRA
        tvWeather.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent=Intent(this@HomeActivity,MainActivity::class.java)
                startActivity(intent)
                customType(this@HomeActivity,"left-to-right")
            }

        })
        imgSemsiye.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent=Intent(this@HomeActivity,MainActivity::class.java)
                startActivity(intent)
                customType(this@HomeActivity,"left-to-right")

            }
        })
        // BUTONA TIKLANDIKTAN SONRA
        tvCurrency.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent=Intent(this@HomeActivity,CurrencyActivity::class.java)
                startActivity(intent)
                customType(this@HomeActivity,"right-to-left")
            }
        })
        imgExchange.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent=Intent(this@HomeActivity,CurrencyActivity::class.java)
                startActivity(intent)
                customType(this@HomeActivity,"right-to-left")
            }

        })
    }
}
