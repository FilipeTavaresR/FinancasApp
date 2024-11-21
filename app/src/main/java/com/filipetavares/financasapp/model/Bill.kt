package com.filipetavares.financasapp.model

data class Bill(
    val date:String = "",
    val description:String = "",
    val type:String = "",
    val value:Double = 0.0
)
