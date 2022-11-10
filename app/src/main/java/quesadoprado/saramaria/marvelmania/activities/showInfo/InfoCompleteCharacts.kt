package quesadoprado.saramaria.marvelmania.activities.showInfo

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
import quesadoprado.saramaria.marvelmania.adapter.ListComicsAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListSeriesAdapter
import quesadoprado.saramaria.marvelmania.data.characters.Character
import quesadoprado.saramaria.marvelmania.data.comics.ComicsDTO
import quesadoprado.saramaria.marvelmania.data.series.SeriesDTO
import quesadoprado.saramaria.marvelmania.databinding.ActivityInfoCompleteBinding
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class InfoCompleteCharacts : AppCompatActivity() {
    private lateinit var binding: ActivityInfoCompleteBinding
    private lateinit var contexto: Context
    private var database=firebaseDatabase
    private var auth= firebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityInfoCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contexto=this
        //obtenemos los datos del personaje
        val charact=intent?.getParcelableExtra<Character>("charact")

        //sacamos del personaje la url para su imagen
        val imageUrl="${charact?.thumbnail?.path}/portrait_uncanny.${charact?.thumbnail?.extension}"

        //MOSTRAMOS LOS DATOS

        Glide.with(this).load(imageUrl).apply(RequestOptions().override(500,650)).into(binding.imageIV)

        binding.nombreTV.text=charact?.name

        //si el usuario esta conectado aparece el icono de favoritos
        if(auth.currentUser!=null){
            binding.iconFav.visibility=View.VISIBLE
            //comprobamos si el usuario lo tiene en favoritos
            comprobarSiFavorito(auth.currentUser!!.uid,charact)
            binding.iconFav.setOnClickListener {
                when (binding.iconFav.tag){
                    //el personaje se elimina de favoritos
                    getString(R.string.fav)->{
                        DataBaseUtils.eliminarPersonaje(auth.currentUser!!.uid,charact!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_noadded)
                        binding.iconFav.tag = getString(R.string.nofav)
                    }
                    //el personaje se añade a favoritos
                    getString(R.string.nofav)->{
                        DataBaseUtils.guardarPersonaje(auth.currentUser!!.uid,charact!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_added)
                        binding.iconFav.tag = getString(R.string.fav)
                    }
                }
            }
            //No hay usuario conectado
        }else{
            binding.iconFav.visibility=View.GONE
        }
        //mostrar la descripcion
        if(charact?.description.equals("")){
            binding.descripcionText.text=getString(R.string.noHayDescripcion)
        }else{
            binding.descripcionText.text=charact?.description
        }
        binding.recyclerViewListComics.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        //obtenemos los comics en los que sale el personaje
        obtenerComicsPorPersonajeId(charact?.id!!)
        binding.recyclerViewListSeries.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        //obtenemos las series en las que aparece el personaje
        obtenerSeriesPorPersonajeId(charact.id)
    }

    private fun obtenerSeriesPorPersonajeId(id: Int) {
        RetrofitBroker.getRequestSeriesForCharacterId(
            id,
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

    private fun obtenerComicsPorPersonajeId(id: Int) {
        RetrofitBroker.getRequestComicsForCharacterId(
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
            })

    }

    private fun comprobarSiFavorito(uid: String, charact: Character?) {
        database.collection("users/$uid/characters").document(charact!!.id.toString()).get()
            .addOnCompleteListener{document->
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