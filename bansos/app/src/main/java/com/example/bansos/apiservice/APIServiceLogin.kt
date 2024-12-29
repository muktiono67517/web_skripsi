package com.example.bansos.apiservice

import com.example.bansos.bansos_program.LoginAuth
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface APIServiceLogin {

    // Endpoint yang sesuai dengan server Anda
    @FormUrlEncoded
    @POST("loginauthmobile")  // Pastikan endpoint sesuai dengan server
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginAuth>
}
