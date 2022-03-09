package com.example.kotlinmessenger


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid:String, val username:String,val state: String):Parcelable{
    constructor() : this("","","")
}