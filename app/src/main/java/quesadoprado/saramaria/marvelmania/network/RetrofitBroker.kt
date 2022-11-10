package quesadoprado.saramaria.marvelmania.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                        override fun onResponse(call: Call<String>,response: Response<String>) {
                            onResponse(response.body()!!)
                        }
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            onFailure(t.message!!)
                        }
                    }
                )
        }
        fun getRequestCharactersByName(name:String,onResponse: (resp: String) -> Unit,onFailure: (resp: String) -> Unit){
            val r=RetrofitApi.retrofitService.getCharacterByName(name = name)
            val p= r.enqueue(
                object : Callback<String>{
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        onResponse(response.body()!!)
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
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
        fun getRequestComicByName(title:String,onResponse: (resp: String) -> Unit,onFailure: (resp: String) -> Unit){
            val r=RetrofitApi.retrofitService.getComicsByTittle(title = title)
            var p= r.enqueue(
                object : Callback<String>{
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        onResponse(response.body()!!)
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
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
        fun getRequestSerieByName(title:String,onResponse: (resp: String) -> Unit,onFailure: (resp: String) -> Unit){
            val r=RetrofitApi.retrofitService.getSerieByName(title = title)
            var p= r.enqueue(
                object : Callback<String>{
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        onResponse(response.body()!!)
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        onFailure(t.message!!)
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