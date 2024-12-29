package com.example.bansos.bansos_program



import APIServiceAnakYatimPiatu
import APIServiceAnakYatimPiatuAdmin
import APIServiceBPNT
import APIServiceBPNTAdmin
import APIServiceDisabilitas
import APIServiceDisabilitasAdmin
import APIServiceRegister
import com.example.bansos.apiservice.APIServiceLogin
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val BASE_URL = "http://192.168.46.100:5000/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


// Admin
    val apiServiceBPNTAdmin:APIServiceBPNTAdmin= retrofit.create(APIServiceBPNTAdmin::class.java)
    val apiServiceDisabilitasAdmin:APIServiceDisabilitasAdmin= retrofit.create(APIServiceDisabilitasAdmin::class.java)
    val apiServiceAnakYatimPiatuAdmin:APIServiceAnakYatimPiatuAdmin= retrofit.create(APIServiceAnakYatimPiatuAdmin::class.java)

//    USER
    val apiServiceLogin:APIServiceLogin = retrofit.create(APIServiceLogin::class.java)
    val apiServiceBPNT:APIServiceBPNT= retrofit.create(APIServiceBPNT::class.java)
    val apiServiceDisabilitas:APIServiceDisabilitas= retrofit.create(APIServiceDisabilitas::class.java)
    val apiServiceAnakYatimPiatu:APIServiceAnakYatimPiatu= retrofit.create(APIServiceAnakYatimPiatu::class.java)
    val apiServiceRegister:APIServiceRegister= retrofit.create(APIServiceRegister::class.java)









}
