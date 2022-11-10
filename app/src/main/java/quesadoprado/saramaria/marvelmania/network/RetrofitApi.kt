package quesadoprado.saramaria.marvelmania.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        @Query("hash")hash:String=Constants.hash(),
        @Query("limit")limit:Int=Constants.limit,
        @Query("orderBy")orderby:String=Constants.orderCharact
    ):Call<String>
    @GET("characters")
    fun getCharacterByName(
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("nameStartsWith")name:String
    ):Call<String>
    @GET("characters/{id}/comics")
    fun getComicsForCharacterId(
        @Path("id")id:Int,
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("limit")limit:Int=Constants.limit,
        @Query("orderBy")orderby:String=Constants.orderComics_Series
    ):Call<String>
    @GET("characters/{id}/series")
    fun getSeriesForCharacterId(
        @Path("id")id:Int,
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("limit")limit:Int=Constants.limit,
        @Query("orderBy")orderby:String=Constants.orderComics_Series
    ):Call<String>
    @GET("comics")
    fun getAllComics(
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("limit")limit:Int=Constants.limit,
        @Query("orderBy")orderby:String=Constants.orderComics_Series
    ):Call<String>
    @GET("comics")
    fun getComicsByTittle(
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("titleStartsWith")title:String
    ):Call<String>
    @GET("comics/{id}/characters")
    fun getCharactersForComicId(
        @Path("id")id:Int,
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("limit")limit:Int=Constants.limit,
        @Query("orderBy")orderby:String=Constants.orderCharact
    ):Call<String>
    @GET("series")
    fun getAllSeries(
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("limit")limit:Int=Constants.limit,
        @Query("orderBy")orderby:String=Constants.orderComics_Series
    ):Call<String>
    @GET("series")
    fun getSerieByName(
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("titleStartsWith")title:String
    ):Call<String>
    @GET("series/{id}")
    fun getSerieForId(
        @Path("id")id:Int,
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("limit")limit:Int=Constants.limit
    ):Call<String>
    @GET("series/{id}/characters")
    fun getCharactersForSerieId(
        @Path("id")id:Int,
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("limit")limit:Int=Constants.limit,
        @Query("orderBy")orderby:String=Constants.orderCharact
    ):Call<String>
    @GET("series/{id}/comics")
    fun getComicsForSerieId(
        @Path("id")id:Int,
        @Query("apikey")apikey:String=Constants.API_KEY,
        @Query("ts")ts:String=Constants.timeStamp,
        @Query("hash")hash:String=Constants.hash(),
        @Query("limit")limit:Int=Constants.limit,
        @Query("orderBy")orderby:String=Constants.orderComics_Series
    ):Call<String>

}
object RetrofitApi {
    val retrofitService : RetrofitApiService by lazy {
        retrofit.create(RetrofitApiService::class.java) }
}