package quesadoprado.saramaria.marvelmania.activities.showInfo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.adapter.ListCharactersAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListComicsAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListSeriesAdapter
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.data.comics.ComicsDTO
import quesadoprado.saramaria.marvelmania.data.series.Serie
import quesadoprado.saramaria.marvelmania.data.series.SeriesDTO
import quesadoprado.saramaria.marvelmania.databinding.ActivityInfocompleteseriesBinding
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker

class InfoCompleteSeries:AppCompatActivity() {
    private lateinit var binding: ActivityInfocompleteseriesBinding
    private lateinit var contexto: Context
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityInfocompleteseriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contexto=this

        val serie=intent.getParcelableExtra<Serie>("serie")

        val imageUrl="${serie?.thumbnail?.path}/portrait_uncanny.${serie?.thumbnail?.extension}"

        Glide.with(this).load(imageUrl).apply(RequestOptions().override(500,650)).into(binding.imageIV)

        binding.tituloTV.text=serie?.title
        var endYear:String
        if (serie?.endYear==null){
            endYear="?"
        }else{
            endYear= serie.endYear.toString()
        }
        binding.fechaText.text=serie?.startYear.toString()+"-"+endYear

        if (serie?.description==null){
            binding.descripcionText.text = getString(R.string.noHayDescripcion)
        }else {
            binding.descripcionText.text = serie?.description
        }

        binding.recyclerViewListComics.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        RetrofitBroker.getRequestComicsForSerieId(
            serie?.id!!,
            onResponse = {
                val respuesta=Gson().fromJson(it,ComicsDTO::class.java)
                val comics=respuesta?.data?.results
                val adapter=ListComicsAdapter(comics)
                binding.recyclerViewListComics.adapter=adapter

                adapter.setOnItemClickListener(object :ListComicsAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        val comic=comics?.get(position)
                        var intent= Intent(contexto,InfoCompleteComics::class.java)
                        intent.putExtra("comic",comic)
                        startActivity(intent)
                    }

                })
            }, onFailure = {
                Log.e("ERROR_API",it)
            }
        )
        binding.recyclerViewListCharacters.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        RetrofitBroker.getRequestCharactersForSerieId(
            serie.id,
            onResponse = {
                val respuesta=Gson().fromJson(it, CharactersDTO::class.java)
                val characters=respuesta?.data?.results
                val adapter= ListCharactersAdapter(characters)
                binding.recyclerViewListCharacters.adapter=adapter

                adapter.setOnItemClickListener(object : ListCharactersAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        val character=characters?.get(position)
                        val intent= Intent(contexto,InfoCompleteCharacts::class.java)
                        intent.putExtra("charact",character)
                        startActivity(intent)
                    }

                })
            }, onFailure = {
                Log.e("ERROR_API",it)
            }
        )
        if (serie.previous!=null){
            val url=serie.previous.resourceURI?.split("/")
            val id:Int= url?.last()!!.toInt()
            RetrofitBroker.getRequestSerieId(
                id,
                onResponse = {
                    val respuesta=Gson().fromJson(it,SeriesDTO::class.java)
                    val serie=respuesta.data?.results?.get(0)
                    val imagePrevious="${serie?.thumbnail?.path}/portrait_uncanny.${serie?.thumbnail?.extension}"
                    Glide.with(this).load(imagePrevious).apply(RequestOptions().override(300,450)).into(binding.anteriorImagen)
                    binding.anteriorImagen.setOnClickListener{
                        val intent=Intent(contexto,InfoCompleteSeries::class.java)
                        intent.putExtra("serie",serie)
                        startActivity(intent)
                    }

                    Log.d("prueba","entra en previous")
                }, onFailure = {
                    Log.e("ERROR_API",it)
                }
            )
        }
        if (serie.next!=null){
            val url=serie.next.resourceURI?.split("/")
            val id:Int= url?.last()!!.toInt()
            RetrofitBroker.getRequestSerieId(
                id,
                onResponse = {
                    val respuesta=Gson().fromJson(it,SeriesDTO::class.java)
                    val serie=respuesta.data?.results?.get(0)
                    val imageNext="${serie?.thumbnail?.path}/portrait_uncanny.${serie?.thumbnail?.extension}"
                    Glide.with(this).load(imageNext).apply(RequestOptions().override(300,450)).into(binding.siguienteImagen)
                    binding.siguienteImagen.setOnClickListener{
                        val intent=Intent(contexto,InfoCompleteSeries::class.java)
                        intent.putExtra("serie",serie)
                        startActivity(intent)
                    }
                    Log.d("prueba",serie?.id.toString())
                }, onFailure = {
                    Log.e("ERROR_API",it)
                }
            )
        }
    }
}