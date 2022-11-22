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
import quesadoprado.saramaria.marvelmania.adapter.ListComicsAdapter
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.data.comics.ComicsDTO
import quesadoprado.saramaria.marvelmania.data.series.Serie
import quesadoprado.saramaria.marvelmania.data.series.SeriesDTO
import quesadoprado.saramaria.marvelmania.data.util.Coment
import quesadoprado.saramaria.marvelmania.databinding.ActivityInfocompleteseriesBinding
import quesadoprado.saramaria.marvelmania.interfaces.OnComentClickListener
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils

class InfoCompleteSeries : AppCompatActivity() {
    private lateinit var binding: ActivityInfocompleteseriesBinding
    private lateinit var contexto: Context

    private var database = FirebaseUtils.firebaseDatabase
    private var auth = FirebaseUtils.firebaseAuth

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var coment: Coment? = null
    private var idComentResp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfocompleteseriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contexto = this

        //añadir boton para volver a la biblioteca con el icono personalizado
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home)

        val serie = intent.getParcelableExtra<Serie>("serie")


        val imageUrl = "${serie?.thumbnail?.path}/portrait_uncanny.${serie?.thumbnail?.extension}"

        Glide.with(this).load(imageUrl).apply(RequestOptions().override(500, 650))
            .into(binding.imageIV)

        binding.tituloTV.text = serie?.title
        val endYear: String
        if (serie?.endYear == null) {
            endYear = "?"
        } else {
            endYear = serie.endYear.toString()
        }
        binding.fechaText.text = serie?.startYear.toString() + "-" + endYear

        if (auth.currentUser != null) {
            binding.iconFav.visibility = View.VISIBLE
            //comprobamos si el usuario tiene el comic en favoritos
            comprobarSiFavorito(auth.currentUser!!.uid, serie)
            binding.iconFav.setOnClickListener {
                when (binding.iconFav.tag) {
                    getString(R.string.fav) -> {
                        DataBaseUtils.eliminarSerie(auth.currentUser!!.uid, serie!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_noadded)
                        binding.iconFav.tag = getString(R.string.nofav)
                    }
                    getString(R.string.nofav) -> {
                        DataBaseUtils.guardarSerie(auth.currentUser!!.uid, serie!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_added)
                        binding.iconFav.tag = getString(R.string.fav)
                    }
                }
            }
            //el usuario no esta conectado
        } else {
            binding.iconFav.visibility = View.GONE
        }
        if (serie?.description == null) {
            binding.descripcionText.text = getString(R.string.noHayDescripcion)
        } else {
            binding.descripcionText.text = serie.description
        }

        binding.recyclerViewListComics.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        obtenerComicsPorSerieId(serie?.id!!)
        binding.recyclerViewListCharacters.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        obtenerPersonajesPorIdSerie(serie.id)

        //si la serie tiene next obtenemos su información y mostramos la imagen al usuario
        if (serie.previous != null) {
            binding.prevNotDataFound.visibility = View.GONE

            val url = serie.previous.resourceURI?.split("/")
            val id: Int = url?.last()!!.toInt()
            obtenerSeriePorId(id, "p")
        } else {
            binding.anteriorImagen.visibility = View.GONE
            binding.prevNotDataFound.visibility = View.VISIBLE
            binding.prevNotDataFound.text = getString(R.string.informacionNoEncontrada)
        }
        //si la serie tiene next obtenemos su información y mostramos la imagen al usuario
        if (serie.next != null) {
            binding.nextNotDataFound.visibility = View.GONE

            val url = serie.next.resourceURI?.split("/")
            val id: Int = url?.last()!!.toInt()
            obtenerSeriePorId(id, "n")
        } else {
            binding.siguienteImagen.visibility = View.GONE
            binding.nextNotDataFound.visibility = View.VISIBLE
            binding.nextNotDataFound.text = getString(R.string.informacionNoEncontrada)
        }

        if (auth.currentUser != null) {
            binding.contentComentarios.visibility = View.VISIBLE

            binding.listaComentarios.layoutManager = LinearLayoutManager(contexto)
            updateComentsUI(serie.id)

            binding.btnComent.setOnClickListener {
                obtenerNombreUsuario(serie.id)
                binding.escribirComentario.text = null
                binding.respuestaComent.visibility = View.GONE
            }

        } else {
            binding.contentComentarios.visibility = View.GONE
        }
    }

    private fun ocultarProgressBar() {
        val handler = Handler()
        val runnable = Runnable {
            binding.progressbarCharacters.visibility = View.GONE
            binding.progressbarComics.visibility = View.GONE
        }
        handler.postDelayed(runnable, 200)
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
                            "serie",
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

    private fun obtenerComentarios(id_serie: Int) {
        database.collection("coments").get().addOnCompleteListener { documents ->
            if (documents.isSuccessful) {
                val comentarios = documents.result.documents
                var lista_coments = arrayOf<Coment>()
                for (coment in comentarios) {
                    if (coment.data!!["type"] == "serie") {
                        val id_type = (coment.data!!["id_type"] as Long).toInt()
                        //comprobamos q el comentario corresponda a la serie que esta viendo el usuario
                        if (id_type == id_serie) {
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

    private fun obtenerSeriePorId(id: Int, prevOrNext: String?) {
        RetrofitBroker.getRequestSerieId(
            id,
            onResponse = {
                val respuesta = Gson().fromJson(it, SeriesDTO::class.java)
                val serie = respuesta.data?.results?.get(0)
                if (serie != null) {
                    val imagePrevious =
                        "${serie.thumbnail?.path}/portrait_uncanny.${serie.thumbnail?.extension}"
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
                } else {
                    when (prevOrNext) {
                        "p" -> {
                            binding.anteriorImagen.visibility = View.GONE
                            binding.prevNotDataFound.visibility = View.VISIBLE
                            binding.prevNotDataFound.text =
                                getString(R.string.informacionNoEncontrada)
                        }
                        "n" -> {
                            binding.siguienteImagen.visibility = View.GONE
                            binding.nextNotDataFound.visibility = View.VISIBLE
                            binding.nextNotDataFound.text =
                                getString(R.string.informacionNoEncontrada)
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
                val respuesta = Gson().fromJson(it, ComicsDTO::class.java)
                val comics = respuesta?.data?.results

                ocultarProgressBar()

                if (comics!!.isNotEmpty()) {
                    binding.recyclerViewListComics.visibility = View.VISIBLE
                    binding.comicsNoEncontrados.visibility = View.GONE

                    val adapter = ListComicsAdapter(comics)
                    binding.recyclerViewListComics.adapter = adapter
                    adapter.setOnItemClickListener(object : ListComicsAdapter.onIntemClickListener {
                        override fun onItemClick(position: Int) {
                            val comic = comics?.get(position)
                            val intent = Intent(contexto, InfoCompleteComics::class.java)
                            intent.putExtra("comic", comic)
                            startActivity(intent)
                        }

                    })
                } else {
                    binding.recyclerViewListComics.visibility = View.GONE
                    binding.comicsNoEncontrados.visibility = View.VISIBLE
                    binding.comicsNoEncontrados.text = getString(R.string.informacionNoEncontrada)
                }
            }, onFailure = {
                Log.e("ERROR_API", it)
            }
        )

    }

    private fun obtenerPersonajesPorIdSerie(id: Int) {
        RetrofitBroker.getRequestCharactersForSerieId(
            id,
            onResponse = {
                val respuesta = Gson().fromJson(it, CharactersDTO::class.java)
                val characters = respuesta?.data?.results

                ocultarProgressBar()

                if (characters!!.isNotEmpty()) {
                    binding.recyclerViewListCharacters.visibility = View.VISIBLE
                    binding.CharactersNoEncontrados.visibility = View.GONE

                    val adapter = ListCharactersAdapter(characters)
                    binding.recyclerViewListCharacters.adapter = adapter
                    adapter.setOnItemClickListener(object :
                        ListCharactersAdapter.onIntemClickListener {
                        override fun onItemClick(position: Int) {
                            val character = characters?.get(position)
                            val intent = Intent(contexto, InfoCompleteCharacts::class.java)
                            intent.putExtra("charact", character)
                            startActivity(intent)
                        }

                    })

                } else {
                    binding.recyclerViewListCharacters.visibility = View.GONE
                    binding.CharactersNoEncontrados.visibility = View.VISIBLE
                    binding.CharactersNoEncontrados.text =
                        getString(R.string.informacionNoEncontrada)
                }

            }, onFailure = {
                Log.e("ERROR_API", it)
            }
        )

    }

    private fun comprobarSiFavorito(uid: String, serie: Serie?) {
        database.collection("users/$uid/series").document(serie!!.id.toString()).get()
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