package com.example.kanwal_laptop.test

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class City(var name: String, var lat : Double, var long : Double){
    var latlong : LatLng = LatLng(lat, long)
    var marker : Marker? = null

    override fun toString() : String{
        return name
    }
}