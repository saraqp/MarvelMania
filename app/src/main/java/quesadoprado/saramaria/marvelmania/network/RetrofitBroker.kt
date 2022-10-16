package quesadoprado.saramaria.marvelmania.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitBroker {
    companion object{
        fun getRequestAllCharacters(onResponse:(resp:String)->Unit, onFailure:(resp:String)->Unit){
            var r=RetrofitApi.retrofitService.getAllCharacters()
            var p= r.enqueue(
                object : Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
                    }

                    override fun onResponse(call: Call<String>,response: Response<String>) {
                        onResponse(response.body()!!)
                    }
                }
            )
        }
        fun getRequestAllComics(onResponse:(resp:String)->Unit, onFailure:(resp:String)->Unit){
            var r=RetrofitApi.retrofitService.getAllComics()
            var p= r.enqueue(
                object : Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
                    }

                    override fun onResponse(call: Call<String>,response: Response<String>) {
                        onResponse(response.body()!!)
                    }
                }
            )
        }
        fun getRequestAllSeries(onResponse:(resp:String)->Unit, onFailure:(resp:String)->Unit){
            var r=RetrofitApi.retrofitService.getAllSeries()
            var p= r.enqueue(
                object : Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
                    }

                    override fun onResponse(call: Call<String>,response: Response<String>) {
                        onResponse(response.body()!!)
                    }
                }
            )
        }
    }
}