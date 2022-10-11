package quesadoprado.saramaria.marvelmania.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(Constants.BASE_URL)
    .build()

interface RetrofitApiService{
    @GET("characters")
    fun getAllCharacters(
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash()
    ):Call<String>
}
object RetrofitApi {
    val retrofitService : RetrofitApiService by lazy {
        retrofit.create(RetrofitApiService::class.java) }
}