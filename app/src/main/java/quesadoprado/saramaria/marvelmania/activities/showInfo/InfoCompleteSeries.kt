package quesadoprado.saramaria.marvelmania.activities.showInfo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.adapter.ListCharactersAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListComicsAdapter
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.data.comics.ComicsDTO
import quesadoprado.saramaria.marvelmania.data.series.Serie
import quesadoprado.saramaria.marvelmania.data.series.SeriesDTO
import quesadoprado.saramaria.marvelmania.databinding.ActivityInfocompleteseriesBinding
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils

class InfoCompleteSeries:AppCompatActivity() {
    private lateinit var binding: ActivityInfocompleteseriesBinding
    private lateinit var contexto: Context

    private var database= FirebaseUtils.firebaseDatabase
    private var auth= FirebaseUtils.firebaseAuth
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
        val endYear:String
        if (serie?.endYear==null){
            endYear="?"
        }else{
            endYear= serie.endYear.toString()
        }
        binding.fechaText.text=serie?.startYear.toString()+"-"+endYear

        if (auth.currentUser!=null){
            binding.iconFav.visibility= View.VISIBLE
            //comprobamos si el usuario tiene el comic en favoritos
            comprobarSiFavorito(auth.currentUser!!.uid,serie)
            binding.iconFav.setOnClickListener {
                when(binding.iconFav.tag){
                    getString(R.string.fav)->{
                        DataBaseUtils.eliminarSerie(auth.currentUser!!.uid,serie!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_noadded)
                        binding.iconFav.tag=getString(R.string.nofav)
                    }
                    getString(R.string.nofav)->{
                        DataBaseUtils.guardarSerie(auth.currentUser!!.uid,serie!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_added)
                        binding.iconFav.tag=getString(R.string.fav)
                    }
                }
            }
            //el usuario no esta conectado
        }else{
            binding.iconFav.visibility= View.GONE
        }
        if (serie?.description==null){
            binding.descripcionText.text = getString(R.string.noHayDescripcion)
        }else {
            binding.descripcionText.text = serie.description
        }

        binding.recyclerViewListComics.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        obtenerComicsPorSerieId(serie?.id!!)
        binding.recyclerViewListCharacters.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        obtenerPersonajesPorIdSerie(serie.id)

        //si la serie tiene next obtenemos su información y mostramos la imagen al usuario
        if (serie.previous!=null){
            val url=serie.previous.resourceURI?.split("/")
            val id:Int= url?.last()!!.toInt()
            obtenerSeriePorId(id,"p")
        }
        //si la serie tiene next obtenemos su información y mostramos la imagen al usuario
        if (serie.next!=null){
            val url=serie.next.resourceURI?.split("/")
            val id:Int= url?.last()!!.toInt()
            obtenerSeriePorId(id,"n")
        }
    }

    private fun obtenerSeriePorId(id: Int, prevOrNext: String?) {
        RetrofitBroker.getRequestSerieId(
            id,
            onResponse = {
                val respuesta = Gson().fromJson(it, SeriesDTO::class.java)
                val serie = respuesta.data?.results?.get(0)
                val imagePrevious =
                    "${serie?.thumbnail?.path}/portrait_uncanny.${serie?.thumbnail?.extension}"
                when (prevOrNext) {
                    "p" -> {
                        Glide.with(this).load(imagePrevious)
                            .apply(RequestOptions().override(300, 450))
                            .into(binding.anteriorImagen)
                        binding.anteriorImagen.setOnClickListener {
                            val intent = Intent(contexto, InfoCompleteSeries::class.java)
                            intent.putExtra("serie", serie)
                            startActivity(intent)
                        }
                    }
                    "n" -> {
                        Glide.with(this).load(imagePrevious)
                            .apply(RequestOptions().override(300, 450))
                            .into(binding.siguienteImagen)
                        binding.siguienteImagen.setOnClickListener {
                            val intent = Intent(contexto, InfoCompleteSeries::class.java)
                            intent.putExtra("serie", serie)
                            startActivity(intent)
                        }
                    }
                }
            }, onFailure = {
                Log.e("ERROR_API", it)
            }
        )
    }

    private fun obtenerComicsPorSerieId(id: Int) {
        RetrofitBroker.getRequestComicsForSerieId(
            id,
            onResponse = {
                val respuesta=Gson().fromJson(it,ComicsDTO::class.java)
                val comics=respuesta?.data?.results
                val adapter=ListComicsAdapter(comics)
                binding.recyclerViewListComics.adapter=adapter

                adapter.setOnItemClickListener(object :ListComicsAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        val comic=comics?.get(position)
                        val intent= Intent(contexto,InfoCompleteComics::class.java)
                        intent.putExtra("comic",comic)
                        startActivity(intent)
                    }

                })
            }, onFailure = {
                Log.e("ERROR_API",it)
            }
        )

    }

    private fun obtenerPersonajesPorIdSerie(id: Int) {
        RetrofitBroker.getRequestCharactersForSerieId(
            id,
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

    }

    private fun comprobarSiFavorito(uid: String, serie: Serie?) {
        database.collection("users/$uid/series").document(serie!!.id.toString()).get()
            .addOnCompleteListener {document->
                if (document.isSuccessful){
                    if (document.result.exists()){
                        binding.iconFav.setImageResource(R.drawable.ic_fav_added)
                        binding.iconFav.tag = getString(R.string.fav)
                    }else{
                        binding.iconFav.setImageResource(R.drawable.ic_fav_noadded)
                        binding.iconFav.tag = getString(R.string.nofav)
                    }
                }
            }

    }
}