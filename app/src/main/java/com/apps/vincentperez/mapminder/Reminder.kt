package com.apps.vincentperez.mapminder

class Reminder {
    var conID:Long?=null
    var Title:String?=null
    var Address:String?=null
    var Latitude:Double?=null
    var Longitude:Double?=null
    var Content:String?=null


    constructor(id:Long,title:String,address:String,lat:Double,lon:Double,content:String){

        this.conID = id
        this.Title = title
        this.Address = address
        this.Latitude = lat
        this.Longitude = lon
        this.Content = content

    }
}