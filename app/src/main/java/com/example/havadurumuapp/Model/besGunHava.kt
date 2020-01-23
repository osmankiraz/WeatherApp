package com.example.havadurumuapp.Model

class besGunHava {

    var gunAdi:String?=null
    var gunAyNumber:String?=null
    var accuIcon:Int?=null
    var maximumTemp:Int?=null
    var minimumTemp:Int?=null

    constructor(){}
    constructor(
        gunAdi: String?,
        gunAyNumber: String?,
        accuIcon: Int?,
        maximumTemp: Int?,
        minimumTemp: Int?
    ) {
        this.gunAdi = gunAdi
        this.gunAyNumber = gunAyNumber
        this.accuIcon = accuIcon
        this.maximumTemp = maximumTemp
        this.minimumTemp = minimumTemp
    }


}