package com.zakia.idn.ojolapp.network

import com.zakia.idn.ojolapp.data.ResultRoute
import io.reactivex.Flowable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("json")
    fun actionRoute(@Query("origin")origin: String,
                    @Query("destination")destination: String,
                    @Query("key")key: String): Flowable<ResultRoute>

    @Headers(
        "Authorization: key=AAAAjU6kaIk:APA91bFi3wz0akR83_wur503chYjy7z0sBCZWYy0CxIboyYuf-33kNWbE8HSBaiDHHA3rA-SGel3EVbYa6qtTB2f81snGGnjlbETbtqGYXhkCE7z8sBFycixcUA2DeGFhDNJrX1oibX1",
        "Content-Type:application/json"
    )
    @POST("fcm/send")
    fun sendChatNotification(@Body requestNotificaton: RequestNotification): Call<ResponseBody>
}