package com.cesar.joel.gurrola.Util

import com.cesar.joel.gurrola.model.ModeloRespuestaApi
import com.google.gson.GsonBuilder
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface ApiInterface {

    @Headers("\"Content-Type: application/json\",  \"Accept: application/json;charset=utf-8\"")
    @POST("Demo/Tracker.Procesos.svc/getConjuntotiendasUsuario")
    fun obtenerData(@Body body:JSONObject):Call<ModeloRespuestaApi>

    companion object Factory {
        val BASE_URL = URLManager().base_url

        val gson = GsonBuilder()
            .setLenient()
            .create()

        fun create(): ApiInterface {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            return retrofit.create(ApiInterface::class.java)
        }

    }

}