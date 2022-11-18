package quesadoprado.saramaria.marvelmania.activities.showInfo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.adapter.ComentAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListComicsAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListSeriesAdapter
import quesadoprado.saramaria.marvelmania.data.characters.Character
import quesadoprado.saramaria.marvelmania.data.comics.ComicsDTO
import quesadoprado.saramaria.marvelmania.data.series.SeriesDTO
import quesadoprado.saramaria.marvelmania.data.util.Coment
import quesadoprado.saramaria.marvelmania.databinding.ActivityInfoCompleteBinding
import quesadoprado.saramaria.marvelmania.interfaces.OnComentClickListener
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class InfoCompleteCharacts : AppCompatActivity() {
    private lateinit var binding: ActivityInfoCompleteBinding
    private lateinit var contexto: Context

    private var database=firebaseDatabase
    private var auth= firebaseAuth

    private val handler=Handler(Looper.getMainLooper())
    private lateinit var runnable:Runnable

    private var coment: Coment?=null
    private var idComentResp:String?=null
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

        if (auth.currentUser!=null){
            binding.contentComentarios.visibility=View.VISIBLE

            binding.listaComentarios.layoutManager=LinearLayoutManager(contexto)

            updateComentsUI(charact.id)

            binding.btnComent.setOnClickListener {
                obtenerNombreUsuario(charact.id)
                binding.escribirComentario.text=null
                binding.respuestaComent.visibility=View.GONE
            }

        }else{
            binding.contentComentarios.visibility=View.GONE
        }
    }
    private fun ocultarProgressBar() {
        val handler= Handler()
        val runnable=Runnable{
            binding.progressbarSeries.visibility=View.GONE
            binding.progressbarComics.visibility=View.GONE
        }
        handler.postDelayed(runnable,200)
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
    private fun updateComentsUI(id: Int) {
        runnable= Runnable {
            obtenerComentarios(id)
            handler.postDelayed(runnable,10000)
        }
        handler.post(runnable)
    }
    private fun obtenerNombreUsuario(id: Int) {
        val comentario_user=binding.escribirComentario.text.toString()
        database.collection("users").document(auth.currentUser!!.uid).get().addOnCompleteListener {task->
            if (task.isSuccessful){
                if (task.result.exists()){
                    val username= task.result.data!!["displayName"] as String
                    coment= Coment("charact",id,username,0,comentario_user,idComentResp,coment?.idComent)
                    if (comentario_user.trim().isNotEmpty()){
                        DataBaseUtils.guardarComentario(coment!!)
                        obtenerComentarios(id)
                    }
                }
            }

        }

    }

    private fun obtenerComentarios(id_serie: Int) {
        database.collection("coments").get().addOnCompleteListener { documents->
            if (documents.isSuccessful){
                val comentarios=documents.result.documents
                var lista_coments= arrayOf<Coment>()
                for (coment in comentarios){
                    if (coment.data!!["type"]=="charact"){
                        val id_type=(coment.data!!["id_type"] as Long).toInt()
                        //comprobamos q el comentario corresponda a la serie que esta viendo el usuario
                        if (id_type==id_serie) {
                            val comentario = Coment(
                                coment.data!!["type"] as String?,
                                (coment.data!!["id_type"] as Long?)?.toInt(),
                                coment.data!!["username"] as String?,
                                (coment.data!!["score"] as Long?)?.toInt(),
                                coment.data!!["coment"] as String?,
                                coment.data!!["id_coment_resp"] as String?,
                                coment.id
                            )
                            lista_coments = lista_coments.plus(comentario)
                        }
                    }
                }
                val adapter= ComentAdapter(lista_coments)

                binding.listaComentarios.adapter=adapter
                adapter.setOnItemClickListener(object : OnComentClickListener {
                    override fun onReplyClick(position: Int) {
                        val comentario= lista_coments[position]
                        idComentResp=comentario.idComent
                        binding.respuestaComent.visibility=View.VISIBLE
                        lista_coments= arrayOf()
                        obtenerComentarioResp()
                        binding.escribirComentario.requestFocus()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onUpVoteClick(position: Int, holder: ImageView, downvote: ImageView) {
                        val coment=lista_coments[position]
                        when(holder.tag){
                            //ya se habia votado
                            getString(R.string.votado)->{
                                //cambiamos el tag a "novotado" y cambiamos el icono a negro
                                holder.tag=getString(R.string.novotado)
                                holder.setImageResource(R.drawable.ic_upvotes)
                                //quitamos su voto
                                DataBaseUtils.cambiarPuntuacionComentario(-1, coment)
                                DataBaseUtils.delVotoUser(coment.idComent)

                                database.collection("coments").document(coment.idComent!!).get()
                                    .addOnSuccessListener { doc->
                                        val puntuacionDoc=doc.data!!["score"].toString().toInt()
                                        coment.puntuacion= puntuacionDoc-1

                                        adapter.notifyDataSetChanged()

                                    }


                            }
                            //no se habia votado
                            getString(R.string.novotado)->{
                                //comprobamos si downvote esta activo
                                if (downvote.tag==getString(R.string.votado)){
                                    holder.tag=getString(R.string.votado)
                                    holder.setImageResource(R.drawable.ic_upvotes_voted)
                                    downvote.tag=getString(R.string.novotado)
                                    downvote.setImageResource(R.drawable.ic_downvotes)


                                    DataBaseUtils.cambiarPuntuacionComentario(2, coment)


                                    DataBaseUtils.addVotoUser("upvote",coment.idComent)

                                    database.collection("coments").document(coment.idComent!!).get()
                                        .addOnSuccessListener { doc->
                                            val puntuacionDoc=doc.data!!["score"].toString().toInt()
                                            coment.puntuacion= puntuacionDoc+ 2

                                            adapter.notifyDataSetChanged()

                                        }



                                }else{
                                    holder.tag=getString(R.string.votado)
                                    holder.setImageResource(R.drawable.ic_upvotes_voted)

                                    DataBaseUtils.cambiarPuntuacionComentario(1, coment)

                                    DataBaseUtils.addVotoUser("upvote",coment.idComent)
                                    database.collection("coments").document(coment.idComent!!).get()
                                        .addOnSuccessListener { doc->
                                            val puntuacionDoc=doc.data!!["score"].toString().toInt()
                                            coment.puntuacion= puntuacionDoc+ 1

                                            adapter.notifyDataSetChanged()

                                        }

                                }
                            }
                        }

                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDownVoteClick(position: Int, holder: ImageView, upvote: ImageView) {
                        val coment=lista_coments[position]
                        when(holder.tag){
                            //ya se habia votado
                            getString(R.string.votado)->{
                                //cambiamos el tag a "novotado" y cambiamos el icono a negro
                                holder.tag=getString(R.string.novotado)
                                holder.setImageResource(R.drawable.ic_downvotes)

                                DataBaseUtils.cambiarPuntuacionComentario(1, coment)

                                DataBaseUtils.delVotoUser(coment.idComent)
                                database.collection("coments").document(coment.idComent!!).get()
                                    .addOnSuccessListener { doc->
                                        val puntuacionDoc=doc.data!!["score"].toString().toInt()
                                        coment.puntuacion= puntuacionDoc+ 1

                                        adapter.notifyDataSetChanged()

                                    }

                            }
                            //no se habia votado
                            getString(R.string.novotado)->{
                                //comprobamos si upvote esta activo
                                if (upvote.tag==getString(R.string.votado)){
                                    holder.tag=getString(R.string.votado)
                                    holder.setImageResource(R.drawable.ic_downvotes_voted)

                                    upvote.tag=getString(R.string.novotado)
                                    upvote.setImageResource(R.drawable.ic_upvotes)

                                    DataBaseUtils.cambiarPuntuacionComentario(-2, coment)

                                    DataBaseUtils.addVotoUser("downvote",coment.idComent)
                                    database.collection("coments").document(coment.idComent!!).get()
                                        .addOnSuccessListener { doc->
                                            val puntuacionDoc=doc.data!!["score"].toString().toInt()
                                            coment.puntuacion= puntuacionDoc- 2

                                            adapter.notifyDataSetChanged()

                                        }


                                }else{
                                    //cambiamos el tag a "votado" y cambiamos el icono a color
                                    holder.tag = getString(R.string.votado)
                                    holder.setImageResource(R.drawable.ic_downvotes_voted)

                                    DataBaseUtils.cambiarPuntuacionComentario(-1,coment)
                                    DataBaseUtils.addVotoUser("downvote",coment.idComent)
                                    database.collection("coments").document(coment.idComent!!).get()
                                        .addOnSuccessListener { doc->
                                            val puntuacionDoc=doc.data!!["score"].toString().toInt()
                                            coment.puntuacion= puntuacionDoc-1

                                            adapter.notifyDataSetChanged()
                                        }

                                }
                            }
                        }
                    }
                    private fun obtenerComentarioResp() {
                        database.collection("coments").document(idComentResp!!).get()
                            .addOnCompleteListener { doc->
                                if (doc.isSuccessful){
                                    val comentario=doc.result.data!!["coment"].toString()
                                    binding.respuestaComent.text=comentario
                                }
                            }
                    }
                })

            }
        }
    }

    private fun obtenerSeriesPorPersonajeId(id: Int) {
        RetrofitBroker.getRequestSeriesForCharacterId(
            id,
            onResponse = {
                val respuesta=Gson().fromJson(it,SeriesDTO::class.java)
                val series=respuesta?.data?.results

                ocultarProgressBar()

                if (series!!.isNotEmpty()){
                    binding.recyclerViewListSeries.visibility=View.VISIBLE
                    binding.seriesNoEncontrados.visibility=View.GONE

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
                }else{
                    binding.recyclerViewListSeries.visibility=View.GONE
                    binding.seriesNoEncontrados.visibility=View.VISIBLE
                    binding.seriesNoEncontrados.text=getString(R.string.informacionNoEncontrada)
                }
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

                ocultarProgressBar()

                if (comics!!.isNotEmpty()){
                    binding.recyclerViewListComics.visibility=View.VISIBLE
                    binding.comicsNoEncontrados.visibility=View.GONE

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
                }else{
                    binding.recyclerViewListComics.visibility=View.GONE
                    binding.comicsNoEncontrados.visibility=View.VISIBLE
                    binding.comicsNoEncontrados.text=getString(R.string.informacionNoEncontrada)
                }
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