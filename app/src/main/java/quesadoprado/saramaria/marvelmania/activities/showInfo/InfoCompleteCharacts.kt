package quesadoprado.saramaria.marvelmania.activities.showInfo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.adapter.ComicAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListComicsAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListSeriesAdapter
import quesadoprado.saramaria.marvelmania.databinding.ActivityInfoCompleteBinding
import quesadoprado.saramaria.marvelmania.data.characters.Character
import quesadoprado.saramaria.marvelmania.data.comics.ComicsDTO
import quesadoprado.saramaria.marvelmania.data.series.SeriesDTO
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker

class InfoCompleteCharacts : AppCompatActivity() {
    private lateinit var binding: ActivityInfoCompleteBinding
    private lateinit var contexto: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityInfoCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contexto=this
        val charact=intent?.getParcelableExtra<Character>("charact")

        val imageUrl="${charact?.thumbnail?.path}/portrait_uncanny.${charact?.thumbnail?.extension}"

        Glide.with(this).load(imageUrl).apply(RequestOptions().override(500,650)).into(binding.imageIV)

        binding.nombreTV.text=charact?.name

        if(charact?.description.equals("")){
            binding.descripcionText.text=getString(R.string.noHayDescripcion)
        }else{
            binding.descripcionText.text=charact?.description
        }

        binding.recyclerViewListComics.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)

        RetrofitBroker.getRequestComicsForCharacterId(
            charact?.id!!,
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
            })

        binding.recyclerViewListSeries.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        RetrofitBroker.getRequestSeriesForCharacterId(
            charact?.id!!,
            onResponse = {
                val respuesta=Gson().fromJson(it,SeriesDTO::class.java)
                val series=respuesta?.data?.results
                val adapter=ListSeriesAdapter(series)
                binding.recyclerViewListSeries.adapter=adapter
                adapter.setOnItemClickListener(object : ListSeriesAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        val serie=series?.get(position)
                        val intent=Intent(contexto,InfoCompleteSeries::class.java)
                        intent.putExtra("serie",serie)
                        startActivity(intent)
                    }

                })
            }, onFailure = {
                Log.e("ERROR_API",it)
            }
        )

    }
}