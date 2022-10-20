package quesadoprado.saramaria.marvelmania.network

import quesadoprado.saramaria.marvelmania.data.comics.Comic
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitBroker {
    companion object{
        fun getRequestAllCharacters(onResponse:(resp:String)->Unit, onFailure:(resp:String)->Unit){
            val r=RetrofitApi.retrofitService.getAllCharacters()
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

        fun getRequestComicsForCharacterId(id:Int,onResponse: (resp: String) -> Unit,onFailure: (resp: String) -> Unit){
            val r=RetrofitApi.retrofitService.getComicsForCharacterId(id)
            var p=r.enqueue(
                object :Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        onResponse(response.body()!!)
                    }
                }
            )
        }

        fun getRequestSeriesForCharacterId(id:Int,onResponse: (resp: String) -> Unit,onFailure: (resp: String) -> Unit){
            val r=RetrofitApi.retrofitService.getSeriesForCharacterId(id)
            var p=r.enqueue(
                object :Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        onResponse(response.body()!!)
                    }
                }
            )
        }
        fun getRequestAllComics(onResponse:(resp:String)->Unit, onFailure:(resp:String)->Unit){
            val r=RetrofitApi.retrofitService.getAllComics()
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
        fun getRequestCharactersForComicId(id:Int,onResponse: (resp: String) -> Unit,onFailure: (resp: String) -> Unit){
            val r=RetrofitApi.retrofitService.getCharactersForComicId(id)
            var p=r.enqueue(
                object :Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        onResponse(response.body()!!)
                    }
                }
            )
        }

        fun getRequestAllSeries(onResponse:(resp:String)->Unit, onFailure:(resp:String)->Unit){
            val r=RetrofitApi.retrofitService.getAllSeries()
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
        fun getRequestSerieId(id:Int,onResponse: (resp: String) -> Unit,onFailure: (resp: String) -> Unit){
            val r=RetrofitApi.retrofitService.getSerieForId(id)
            var p=r.enqueue(
                object :Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        onResponse(response.body()!!)
                    }
                }
            )
        }
        fun getRequestComicsForSerieId(id:Int,onResponse: (resp: String) -> Unit,onFailure: (resp: String) -> Unit){
            val r=RetrofitApi.retrofitService.getComicsForSerieId(id)
            var p=r.enqueue(
                object :Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        onResponse(response.body()!!)
                    }
                }
            )
        }
        fun getRequestCharactersForSerieId(id:Int,onResponse: (resp: String) -> Unit,onFailure: (resp: String) -> Unit){
            val r=RetrofitApi.retrofitService.getCharactersForSerieId(id)
            var p=r.enqueue(
                object :Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        onResponse(response.body()!!)
                    }
                }
            )
        }

    }
}