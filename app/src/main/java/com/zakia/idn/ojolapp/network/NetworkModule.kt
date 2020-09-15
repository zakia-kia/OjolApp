package com.zakia.idn.ojolapp.network

import com.zakia.idn.ojolapp.utils.Constan
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    fun getOkhttp(): OkHttpClient {

        val log2 = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(log2).build()

        return client
    }

    fun getRetrofit(): Retrofit {
        var retrofit = Retrofit.Builder().baseUrl(Constan.BaseUrlRoute)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(getOkhttp()).build()

        return retrofit
    }

    fun getRetrofitFcm(): Retrofit {
        var retrofit = Retrofit.Builder().baseUrl(Constan.BaseUrlFcm)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkhttp()).build()

        return retrofit
    }

    fun getService(): ApiService{
        var service: ApiService = getRetrofit().create(ApiService::class.java)
        return service
    }

    fun getServiceFcm(): ApiService{
        var service: ApiService = getRetrofitFcm().create(ApiService::class.java)
        return service
    }
}