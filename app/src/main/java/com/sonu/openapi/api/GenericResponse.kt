package com.sonu.openapi.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GenericResponse(
    @SerializedName("response")
    @Expose
    var response: String,

    @SerializedName("error_message")
    @Expose
    val msg: String ? = null

)