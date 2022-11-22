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
import quesadoprado.saramaria.marvelmania.adapter.ListCharactersAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListVariantImagesComicsAdapter
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.data.util.Coment
import quesadoprado.saramaria.marvelmania.databinding.ActivityInfocompletecomicsBinding
import quesadoprado.saramaria.marvelmania.interfaces.OnComentClickListener
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class InfoCompleteComics : AppCompatActivity() {
    private lateinit var binding: ActivityInfocompletecomicsBinding
    private lateinit var context: Context
    private var database = firebaseDatabase
    private var auth = firebaseAuth

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var coment: Coment? = null
    private var idComentResp: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfocompletecomicsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        //añadir boton para volver a la biblioteca con el icono personalizado
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home)
        //obtenemos los datos del comic
        val comic = intent?.getParcelableExtra<Comic>("comic")
        //obtenemos la url de la imagen del personaje
        val imageUrl = "${comic?.thumbnail?.path}/portrait_uncanny.${comic?.thumbnail?.extension}"

        //MOSTRAMOS LOS DATOS

        Glide.with(this).load(imageUrl).apply(RequestOptions().override(500, 650))
            .into(binding.imageIV)

        binding.tituloTV.text = comic?.title

        //comprobamos si el usuario esta conectado para que le aparezca (o no) el icono de favoritos
        if (auth.currentUser != null) {
            binding.iconFav.visibility = View.VISIBLE
            //comprobamos si el usuario tiene el comic en favoritos
            comprobarSiFavorito(auth.currentUser!!.uid, comic)
            binding.iconFav.setOnClickListener {
                when (binding.iconFav.tag) {
                    getString(R.string.fav) -> {
                        DataBaseUtils.eliminarComic(auth.currentUser!!.uid, comic!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_noadded)
                        binding.iconFav.tag = getString(R.string.nofav)
                    }
                    getString(R.string.nofav) -> {
                        DataBaseUtils.guardarComic(auth.currentUser!!.uid, comic!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_added)
                        binding.iconFav.tag = getString(R.string.fav)
                    }
                }
            }
            //el usuario no esta conectado
        } else {
            binding.iconFav.visibility = View.GONE
        }
        if (comic?.description.equals("") && comic?.variantDescription.equals("")) {
            binding.descripcionText.text = getString(R.string.noHayDescripcion)
        } else if (comic?.description.equals("")) {
            binding.descripcionText.text = comic?.variantDescription
        } else {
            binding.descripcionText.text = comic?.description
        }
        binding.numpagText.text = comic?.pageCount.toString()
        binding.formatText.text = comic?.format

        //Mostramos sus imagenes variantes
        binding.recyclerViewListVariantImages.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val adapterimages = ListVariantImagesComicsAdapter(comic?.images)
        binding.recyclerViewListVariantImages.adapter = adapterimages

        //Mostramos sus personajes
        binding.recyclerViewListCharacters.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        obtenerPersonajesPorComicId(comic?.id!!)

        if (auth.currentUser != null) {
            binding.contentComentarios.visibility = View.VISIBLE

            binding.listaComentarios.layoutManager = LinearLayoutManager(context)
            updateComentsUI(comic.id)
            binding.btnComent.setOnClickListener {
                obtenerNombreUsuario(comic.id)
                binding.respuestaComent.visibility = View.GONE
                binding.escribirComentario.text = null
            }
        } else {
            binding.contentComentarios.visibility = View.GONE
        }
    }

    private fun updateComentsUI(id: Int) {
        runnable = Runnable {
            obtenerComentarios(id)
            handler.postDelayed(runnable, 60000)
        }
        handler.post(runnable)
    }

    private fun obtenerNombreUsuario(id: Int) {
        val comentario_user = binding.escribirComentario.text.toString()
        database.collection("users").document(auth.currentUser!!.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.exists()) {
                        val username = task.result.data!!["displayName"] as String
                        coment = Coment(
                            "comic",
                            id,
                            username,
                            0,
                            comentario_user,
                            idComentResp,
                            coment?.idComent
                        )
                        if (comentario_user.trim().isNotEmpty()) {
                            DataBaseUtils.guardarComentario(coment!!)
                            obtenerComentarios(id)
                        }
                    }
                }

            }
    }

    private fun ocultarProgressBar() {
        val handler = Handler()
        val runnable = Runnable {
            binding.progressbarCharacters.visibility = View.GONE
        }
        handler.postDelayed(runnable, 200)
    }

    private fun obtenerComentarios(id: Int) {
        database.collection("coments").get().addOnCompleteListener { document ->
            if (document.isSuccessful) {
                val comentarios = document.result.documents
                var lista_coments = arrayOf<Coment>()
                for (coment in comentarios) {
                    if (coment.data!!["type"] == "comic") {
                        val id_type = (coment.data!!["id_type"] as Long).toInt()
                        //comprobamos q el comentario corresponda con el comic visualizado
                        if (id_type == id) {
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
                val adapter = ComentAdapter(lista_coments)

                binding.listaComentarios.adapter = adapter
                adapter.setOnItemClickListener(object : OnComentClickListener {
                    override fun onReplyClick(position: Int) {
                        idComentResp = lista_coments[position].idComent
                        binding.respuestaComent.visibility = View.VISIBLE
                        obtenerComentarioResp()
                        binding.escribirComentario.requestFocus()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onUpVoteClick(
                        position: Int,
                        holder: ImageView,
                        downvote: ImageView
                    ) {
                        val coment = lista_coments[position]
                        when (holder.tag) {
                            //ya se habia votado
                            getString(R.string.votado) -> {
                                //cambiamos el tag a "novotado" y cambiamos el icono a negro
                                holder.tag = getString(R.string.novotado)
                                holder.setImageResource(R.drawable.ic_upvotes)
                                //quitamos su voto
                                DataBaseUtils.cambiarPuntuacionComentario(-1, coment)
                                DataBaseUtils.delVotoUser(coment.idComent)

                                database.collection("coments").document(coment.idComent!!).get()
                                    .addOnSuccessListener { doc ->
                                        val puntuacionDoc = doc.data!!["score"].toString().toInt()
                                        coment.puntuacion = puntuacionDoc - 1
                                        adapter.notifyDataSetChanged()
                                    }
                            }
                            //no se habia votado
                            getString(R.string.novotado) -> {
                                //comprobamos si downvote esta activo
                                if (downvote.tag == getString(R.string.votado)) {
                                    holder.tag = getString(R.string.votado)
                                    holder.setImageResource(R.drawable.ic_upvotes_voted)
                                    downvote.tag = getString(R.string.novotado)
                                    downvote.setImageResource(R.drawable.ic_downvotes)


                                    DataBaseUtils.cambiarPuntuacionComentario(2, coment)


                                    DataBaseUtils.addVotoUser("upvote", coment.idComent)

                                    database.collection("coments").document(coment.idComent!!).get()
                                        .addOnSuccessListener { doc ->
                                            val puntuacionDoc =
                                                doc.data!!["score"].toString().toInt()
                                            coment.puntuacion = puntuacionDoc + 2
                                            adapter.notifyDataSetChanged()
                                        }
                                } else {
                                    holder.tag = getString(R.string.votado)
                                    holder.setImageResource(R.drawable.ic_upvotes_voted)

                                    DataBaseUtils.cambiarPuntuacionComentario(1, coment)

                                    DataBaseUtils.addVotoUser("upvote", coment.idComent)
                                    database.collection("coments").document(coment.idComent!!).get()
                                        .addOnSuccessListener { doc ->
                                            val puntuacionDoc =
                                                doc.data!!["score"].toString().toInt()
                                            coment.puntuacion = puntuacionDoc + 1
                                            adapter.notifyDataSetChanged()
                                        }
                                }
                            }
                        }
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDownVoteClick(
                        position: Int,
                        holder: ImageView,
                        upvote: ImageView
                    ) {
                        val coment = lista_coments[position]
                        when (holder.tag) {
                            //ya se habia votado
                            getString(R.string.votado) -> {
                                //cambiamos el tag a "novotado" y cambiamos el icono a negro
                                holder.tag = getString(R.string.novotado)
                                holder.setImageResource(R.drawable.ic_downvotes)

                                DataBaseUtils.cambiarPuntuacionComentario(1, coment)

                                DataBaseUtils.delVotoUser(coment.idComent)
                                database.collection("coments").document(coment.idComent!!).get()
                                    .addOnSuccessListener { doc ->
                                        val puntuacionDoc = doc.data!!["score"].toString().toInt()
                                        coment.puntuacion = puntuacionDoc + 1
                                        adapter.notifyDataSetChanged()
                                    }

                            }
                            //no se habia votado
                            getString(R.string.novotado) -> {
                                //comprobamos si upvote esta activo
                                if (upvote.tag == getString(R.string.votado)) {
                                    holder.tag = getString(R.string.votado)
                                    holder.setImageResource(R.drawable.ic_downvotes_voted)

                                    upvote.tag = getString(R.string.novotado)
                                    upvote.setImageResource(R.drawable.ic_upvotes)

                                    DataBaseUtils.cambiarPuntuacionComentario(-2, coment)

                                    DataBaseUtils.addVotoUser("downvote", coment.idComent)
                                    database.collection("coments").document(coment.idComent!!).get()
                                        .addOnSuccessListener { doc ->
                                            val puntuacionDoc =
                                                doc.data!!["score"].toString().toInt()
                                            coment.puntuacion = puntuacionDoc - 2
                                            adapter.notifyDataSetChanged()
                                        }


                                } else {
                                    //cambiamos el tag a "votado" y cambiamos el icono a color
                                    holder.tag = getString(R.string.votado)
                                    holder.setImageResource(R.drawable.ic_downvotes_voted)

                                    DataBaseUtils.cambiarPuntuacionComentario(-1, coment)
                                    DataBaseUtils.addVotoUser("downvote", coment.idComent)
                                    database.collection("coments").document(coment.idComent!!).get()
                                        .addOnSuccessListener { doc ->
                                            val puntuacionDoc =
                                                doc.data!!["score"].toString().toInt()
                                            coment.puntuacion = puntuacionDoc - 1
                                            adapter.notifyDataSetChanged()
                                        }

                                }
                            }
                        }
                    }

                    private fun obtenerComentarioResp() {
                        database.collection("coments").document(idComentResp!!).get()
                            .addOnCompleteListener { doc ->
                                if (doc.isSuccessful) {
                                    val comentario = doc.result.data!!["coment"].toString()
                                    binding.respuestaComent.text = comentario
                                }
                            }
                    }

                })
            }
        }
    }

    private fun obtenerPersonajesPorComicId(id: Int) {
        RetrofitBroker.getRequestCharactersForComicId(
            id,
            onResponse = {
                val respuesta = Gson().fromJson(it, CharactersDTO::class.java)
                val characters = respuesta?.data?.results

                ocultarProgressBar()

                if (characters!!.isNotEmpty()) {
                    binding.recyclerViewListCharacters.visibility = View.VISIBLE
                    binding.charactersNoEncontrados.visibility = View.GONE

                    val adapter = ListCharactersAdapter(characters)
                    binding.recyclerViewListCharacters.adapter = adapter
                    adapter.setOnItemClickListener(object :
                        ListCharactersAdapter.onIntemClickListener {
                        override fun onItemClick(position: Int) {
                            val character = characters?.get(position)
                            val intent = Intent(context, InfoCompleteCharacts::class.java)
                            intent.putExtra("charact", character)
                            startActivity(intent)
                        }

                    })

                } else {
                    binding.recyclerViewListCharacters.visibility = View.GONE
                    binding.charactersNoEncontrados.visibility = View.VISIBLE
                    binding.charactersNoEncontrados.text =
                        getString(R.string.informacionNoEncontrada)
                }

            }, onFailure = {
                Log.e("ERROR_API", it)
            }
        )
    }

    private fun comprobarSiFavorito(uid: String, comic: Comic?) {
        database.collection("users/$uid/comics").document(comic!!.id.toString()).get()
            .addOnCompleteListener { document ->
                if (document.isSuccessful) {
                    if (document.result.exists()) {
                        binding.iconFav.setImageResource(R.drawable.ic_fav_added)
                        binding.iconFav.tag = getString(R.string.fav)
                    } else {
                        binding.iconFav.setImageResource(R.drawable.ic_fav_noadded)
                        binding.iconFav.tag = getString(R.string.nofav)
                    }
                }
            }

    }
}