package com.zakia.idn.ojolapp.network

import com.google.gson.annotations.SerializedName
import com.zakia.idn.ojolapp.model.Booking

class RequestNotification {

    @SerializedName("o")
    var token: String? = null

    @SerializedName("data")
    var sendNotificationModel: Booking? = null
}