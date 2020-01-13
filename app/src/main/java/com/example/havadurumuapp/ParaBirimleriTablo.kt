package com.example.havadurumuapp

import java.util.*

data class ParaBirimleriTablo (var id:Int=0,var dollar:String="",var euro: String="",var date:String="")
data class EuroTablo (var id:Int=0,var euro:String="",var date:String="")
data class WeatherTablo (var id:Int =0,var city:String="",var temp:String="",var description:String="",var date:String="",var icon:String="")