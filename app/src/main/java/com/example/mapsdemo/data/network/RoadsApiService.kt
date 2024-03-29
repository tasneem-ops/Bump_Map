//package com.example.mapsdemo.data.network
//
//import com.example.asteroid_radar_app.Constants
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
//import kotlinx.coroutines.Deferred
//import okhttp3.OkHttpClient
//import org.json.JSONObject
//import retrofit2.Retrofit
//import retrofit2.converter.moshi.MoshiConverterFactory
//import retrofit2.converter.scalars.ScalarsConverterFactory
//import retrofit2.http.GET
//import java.text.SimpleDateFormat
//import java.util.*
//import java.util.concurrent.TimeUnit
//
//private const val BASE_URL = "https://api.nasa.gov/"
//val today = NetworkUtils().today
//val lastDay = NetworkUtils().lastDay
//val networkUtils = NetworkUtils()
//val client = OkHttpClient.Builder()
//    .connectTimeout(10, TimeUnit.MINUTES)
//    .writeTimeout(10, TimeUnit.MINUTES) // write timeout
//    .readTimeout(10 , TimeUnit.MINUTES) // read timeout
//    .build()
//private val retrofit = Retrofit.Builder()
//    .addConverterFactory(ScalarsConverterFactory.create())
//    .baseUrl(BASE_URL)
//    .client(client)
//    .build()
//
//interface RoadsApiService{
//
//    @GET ("neo/rest/v1/feed?start_date=today&end_date=lastDay&api_key=${Constants.API_KEY}")
//    suspend fun getProperties(): String
//
//}
//
//object RoadsApi {
//    val retrofitService : RoadsApiService by lazy {
//        retrofit.create(RoadsApiService::class.java)
//    }
//}
//
//
