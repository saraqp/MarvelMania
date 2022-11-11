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
import quesadoprado.saramaria.marvelmania.adapter.ListCharactersAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListVariantImagesComicsAdapter
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.databinding.ActivityInfocompletecomicsBinding
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class InfoCompleteComics:AppCompatActivity() {
    private lateinit var binding: ActivityInfocompletecomicsBinding
    private lateinit var context : Context
    private var database=firebaseDatabase
    private var auth=firebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfocompletecomicsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context=this

        //obtenemos los datos del comic
        val comic=intent?.getParcelableExtra<Comic>("comic")
        //obtenemos la url de la imagen del personaje
        val imageUrl="${comic?.thumbnail?.path}/portrait_uncanny.${comic?.thumbnail?.extension}"

        //MOSTRAMOS LOS DATOS

        Glide.with(this).load(imageUrl).apply(RequestOptions().override(500,650)).into(binding.imageIV)

        binding.tituloTV.text=comic?.title

        //comprobamos si el usuario esta conectado para que le aparezca (o no) el icono de favoritos
        if (auth.currentUser!=null){
            binding.iconFav.visibility=View.VISIBLE
            //comprobamos si el usuario tiene el comic en favoritos
            comprobarSiFavorito(auth.currentUser!!.uid,comic)
            binding.iconFav.setOnClickListener {
                when(binding.iconFav.tag){
                    getString(R.string.fav)->{
                        DataBaseUtils.eliminarComic(auth.currentUser!!.uid,comic!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_noadded)
                        binding.iconFav.tag=getString(R.string.nofav)
                    }
                    getString(R.string.nofav)->{
                        DataBaseUtils.guardarComic(auth.currentUser!!.uid,comic!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_added)
                        binding.iconFav.tag=getString(R.string.fav)
                    }
                }
            }
            //el usuario no esta conectado
        }else{
            binding.iconFav.visibility=View.GONE
        }
        if(comic?.description.equals("")&& comic?.variantDescription.equals("")){
            binding.descripcionText.text=getString(R.string.noHayDescripcion)
        }else if(comic?.description.equals("")){
            binding.descripcionText.text=comic?.variantDescription
        }else{
            binding.descripcionText.text=comic?.description
        }
        binding.numpagText.text= comic?.pageCount.toString()
        binding.formatText.text=comic?.format

        //Mostramos sus imagenes variantes
        binding.recyclerViewListVariantImages.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        val adapterimages=ListVariantImagesComicsAdapter(comic?.images)
        binding.recyclerViewListVariantImages.adapter=adapterimages

        //Mostramos sus personajes
        binding.recyclerViewListCharacters.layoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
        obtenerPersonajesPorComicId(comic?.id!!)
    }

    private fun obtenerPersonajesPorComicId(id: Int) {
        RetrofitBroker.getRequestCharactersForComicId(
            id,
            onResponse = {
                val respuesta=Gson().fromJson(it,CharactersDTO::class.java)
                val characters=respuesta?.data?.results
                val adapter=ListCharactersAdapter(characters)
                binding.recyclerViewListCharacters.adapter=adapter

                adapter.setOnItemClickListener(object : ListCharactersAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        val character=characters?.get(position)
                        val intent= Intent(context,InfoCompleteCharacts::class.java)
                        intent.putExtra("charact",character)
                        startActivity(intent)
                    }

                })
            }, onFailure = {
                Log.e("ERROR_API",it)
            }
        )
    }

    private fun comprobarSiFavorito(uid: String, comic: Comic?) {
        database.collection("users/$uid/comics").document(comic!!.id.toString()).get()
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