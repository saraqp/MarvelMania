package quesadoprado.saramaria.marvelmania.activities.showInfo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
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
import quesadoprado.saramaria.marvelmania.fragments.*
import quesadoprado.saramaria.marvelmania.interfaces.OnComentClickListener
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class InfoCompleteCharacts : AppCompatActivity() {
    private lateinit var binding: ActivityInfoCompleteBinding
    private lateinit var contexto: Context

    private var database = firebaseDatabase
    private var auth = firebaseAuth


    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var coment: Coment? = null
    private var idComentResp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contexto = this
        //a??adir boton para volver a la biblioteca con el icono personalizado
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home)
        //obtenemos los datos del personaje
        val charact = intent?.getParcelableExtra<Character>("charact")

        //sacamos del personaje la url para su imagen
        val imageUrl =
            "${charact?.thumbnail?.path}/portrait_uncanny.${charact?.thumbnail?.extension}"

        //MOSTRAMOS LOS DATOS

        Glide.with(this).load(imageUrl).apply(RequestOptions().override(500, 650))
            .into(binding.imageIV)

        binding.nombreTV.text = charact?.name

        //si el usuario esta conectado aparece el icono de favoritos
        if (auth.currentUser != null) {
            binding.iconFav.visibility = View.VISIBLE
            //comprobamos si el usuario lo tiene en favoritos
            comprobarSiFavorito(auth.currentUser!!.uid, charact)
            binding.iconFav.setOnClickListener {
                when (binding.iconFav.tag) {
                    //el personaje se elimina de favoritos
                    getString(R.string.fav) -> {
                        DataBaseUtils.eliminarPersonaje(auth.currentUser!!.uid, charact!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_noadded)
                        binding.iconFav.tag = getString(R.string.nofav)
                    }
                    //el personaje se a??ade a favoritos
                    getString(R.string.nofav) -> {
                        DataBaseUtils.guardarPersonaje(auth.currentUser!!.uid, charact!!)
                        binding.iconFav.setImageResource(R.drawable.ic_fav_added)
                        binding.iconFav.tag = getString(R.string.fav)
                    }
                }
            }
            //No hay usuario conectado
        } else {
            binding.iconFav.visibility = View.GONE
        }
        //mostrar la descripcion
        if (charact?.description.equals("")) {
            binding.descripcionText.text = getString(R.string.noHayDescripcion)
        } else {
            binding.descripcionText.text = charact?.description
        }
        binding.recyclerViewListComics.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        //obtenemos los comics en los que sale el personaje
        obtenerComicsPorPersonajeId(charact?.id!!)

        binding.recyclerViewListSeries.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        //obtenemos las series en las que aparece el personaje
        obtenerSeriesPorPersonajeId(charact.id)

        if (auth.currentUser != null) {
            binding.contentComentarios.visibility = View.VISIBLE

            binding.listaComentarios.layoutManager = LinearLayoutManager(contexto)

            updateComentsUI(charact.id)

            binding.btnComent.setOnClickListener {
                obtenerNombreUsuario(charact.id)
                hideKeyboard()
                binding.escribirComentario.text = null
                binding.respuestaComent.visibility = View.GONE
            }

        } else {
            binding.contentComentarios.visibility = View.GONE
        }
    }

    //OCULTAR TECLADO
    fun AppCompatActivity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun ocultarProgressBar() {
        val handler = Handler()
        val runnable = Runnable {
            binding.progressbarSeries.visibility = View.GONE
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
        val comentUser = binding.escribirComentario.text.toString()
        //obtenemos el nombre de usuario del User que ha escrito el comentario
        database.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { task ->
                if (task.exists()) {
                    val username = task.data!!["displayName"] as String
                    //creamos el comentario con el nombre de usuario y su uid
                    coment = Coment(
                        "charact",
                        id,
                        username,
                        auth.currentUser!!.uid,
                        0,
                        comentUser,
                        idComentResp,
                        coment?.idComent,
                        false
                    )
                    /*guardamos el nombre de usuario y ponemos idComentResp a null por si el mensaje
                    era una respuesta a otro y volvemos a obtener los comentarios para actualizar la
                    lista y que aparezca el nuevo
                     */
                    if (comentUser.trim().isNotEmpty()) {
                        DataBaseUtils.guardarComentario(coment!!)
                        idComentResp = null
                        obtenerComentarios(id)
                    }
                }

            }

    }


    private fun obtenerComentarios(id: Int) {
        database.collection("coments").get().addOnSuccessListener { documents ->
            val comentarios = documents.documents
            var listaComents = arrayOf<Coment>()
            for (coment in comentarios) {
                if (coment.data!!["type"] == "charact") {
                    val id_type = (coment.data!!["id_type"] as Long).toInt()
                    //comprobamos q el comentario corresponda a la serie que esta viendo el usuario
                    if (id_type == id) {
                        val comentario = Coment(
                            coment.data!!["type"] as String,
                            (coment.data!!["id_type"] as Long).toInt(),
                            coment.data!!["username"] as String,
                            coment.data!!["id_userComent"] as String,
                            (coment.data!!["score"] as Long?)?.toInt(),
                            coment.data!!["coment"] as String?,
                            coment.data!!["id_coment_resp"] as String?,
                            coment.id,
                            coment.data!!["edited"] as Boolean
                        )
                        listaComents = listaComents.plus(comentario)
                        listaComents.sortByDescending { it.puntuacion }
                    }
                }
            }
            val adapter = ComentAdapter(listaComents)

            binding.listaComentarios.adapter = adapter
            adapter.setOnItemClickListener(object : OnComentClickListener {
                override fun onReplyClick(position: Int) {
                    idComentResp = listaComents[position].idComent
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
                    val coment = listaComents[position]
                    when (holder.tag) {
                        //ya se habia votado
                        getString(R.string.votado) -> {
                            //cambiamos el tag a "novotado" y cambiamos el icono a negro
                            holder.tag = getString(R.string.novotado)
                            holder.setImageResource(R.drawable.ic_upvotes)
                            //quitamos su voto
                            DataBaseUtils.cambiarPuntuacionComentario(-1, coment)
                            DataBaseUtils.delVotoUser(coment.idComent)
                            //cambiamos visualmente la puntuacion del comentario
                            database.collection("coments").document(coment.idComent!!).get()
                                .addOnSuccessListener { doc ->
                                    val puntuacionDoc = doc.data!!["score"].toString().toInt()
                                    coment.puntuacion = puntuacionDoc - 1

                                    listaComents[position] = coment
                                    adapter.notifyDataSetChanged()

                                }


                        }
                        //no se habia votado
                        getString(R.string.novotado) -> {
                            //comprobamos si downvote esta activo
                            if (downvote.tag == getString(R.string.votado)) {

                                //cambiamos el tag de upvote a "votado" y cambiamos el icono a color
                                holder.tag = getString(R.string.votado)
                                holder.setImageResource(R.drawable.ic_upvotes_voted)

                                //cambiamos el tag de downvote a "novotado" y cambiamos el icono a negro
                                downvote.tag = getString(R.string.novotado)
                                downvote.setImageResource(R.drawable.ic_downvotes)

                                //cambiamos la puntuaci??n en la base de datos
                                /*
                                * Como se est?? cambiando de downvote a upvote ha de sumarse 2 puntos
                                * a la puntuaci??n: +1 para quitar el -1 del downvote y otro +1 para
                                * a??adir el voto de upvote
                                * */
                                DataBaseUtils.cambiarPuntuacionComentario(2, coment)
                                DataBaseUtils.addVotoUser("upvote", coment.idComent)

                                //cambiamos visualmente la puntuaci??n de usuario
                                database.collection("coments").document(coment.idComent!!).get()
                                    .addOnSuccessListener { doc ->
                                        val puntuacionDoc =
                                            doc.data!!["score"].toString().toInt()
                                        coment.puntuacion = puntuacionDoc + 2

                                        listaComents[position] = coment
                                        adapter.notifyDataSetChanged()

                                    }

                                //downvote no esta activo
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

                                        listaComents[position] = coment
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
                    val coment = listaComents[position]
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

                                    listaComents[position] = coment
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

                                        listaComents.set(position, coment)
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
                                        listaComents[position] = coment
                                        adapter.notifyDataSetChanged()
                                    }

                            }
                        }
                    }
                }

                override fun onDeleteClick(position: Int) {
                    val comentario = listaComents[position]

                    //verificamos que el usuario est?? seguro de borrar su comentario
                    val builder = AlertDialog.Builder(contexto)

                    builder.setMessage(getString(R.string.asegurarBorradoComent))
                        .setPositiveButton(getString(R.string.si)) { _, _ ->
                            DataBaseUtils.camiarComentario(
                                comentario,
                                getString(R.string.comentarioBorradomsg)
                            )
                            Snackbar.make(
                                binding.drawerLayout,
                                getString(R.string.comentarioBorrado),
                                Snackbar.LENGTH_SHORT
                            ).show()

                            comentario.comentario = getString(R.string.comentarioBorradomsg)

                            listaComents[position] = comentario
                            adapter.notifyDataSetChanged()
                        }
                        //en caso negativo no hacemos nada
                        .setNegativeButton(getString(R.string.no)) { _, _ -> }
                        .show()
                }

                override fun onEditClick(position: Int) {
                    val coment = listaComents[position]

                    //mostramos un dialog con el texto del comentario que quiere cambiar
                    val dialogEditComent: AlertDialog.Builder = AlertDialog.Builder(contexto)
                    val inflater = layoutInflater
                    val dialogLayout = inflater.inflate(R.layout.dialog_edit_coment, null)
                    val edit = dialogLayout.findViewById<EditText>(R.id.edited_coment)

                    with(dialogEditComent) {
                        setTitle(getString(R.string.editComent))
                        setView(dialogLayout)
                        edit.setText(coment.comentario)
                        setPositiveButton(getString(R.string.editBtn)) { _, _ ->
                            if (edit.text.toString().isNotEmpty()) {
                                DataBaseUtils.camiarComentario(coment, edit.text.toString())

                                coment.comentario = edit.text.toString()
                                coment.edited = true

                                listaComents[position] = coment
                                adapter.notifyDataSetChanged()
                                /*si el edittext est?? vac??o mostramos un mensaje diciendole al usuario
                                * que no puede dejar el campo vac??o
                                * */
                            } else {
                                Snackbar.make(
                                    binding.drawerLayout,
                                    getString(R.string.campoNoVacio),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                        //si cancela no se hace nada
                        setNegativeButton(getString(R.string.cancel)) { _, _ ->
                        }
                        show()
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

    //obtener las series que pertenecen al personaje
    private fun obtenerSeriesPorPersonajeId(id: Int) {
        RetrofitBroker.getRequestSeriesForCharacterId(
            id,
            onResponse = {
                val respuesta = Gson().fromJson(it, SeriesDTO::class.java)
                val series = respuesta?.data?.results

                ocultarProgressBar()

                if (series!!.isNotEmpty()) {
                    binding.recyclerViewListSeries.visibility = View.VISIBLE
                    binding.seriesNoEncontrados.visibility = View.GONE

                    val adapter = ListSeriesAdapter(series)
                    binding.recyclerViewListSeries.adapter = adapter
                    adapter.setOnItemClickListener(object : OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val serie = series?.get(position)
                            val intent = Intent(contexto, InfoCompleteSeries::class.java)
                            intent.putExtra("serie", serie)
                            startActivity(intent)
                        }

                    })
                } else {
                    binding.recyclerViewListSeries.visibility = View.GONE
                    binding.seriesNoEncontrados.visibility = View.VISIBLE
                    binding.seriesNoEncontrados.text = getString(R.string.informacionNoEncontrada)
                }
            }, onFailure = {
                Snackbar.make(
                    binding.drawerLayout,
                    getString(R.string.error),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        )
    }

    //obtener los comics que pertenecen al personaje
    private fun obtenerComicsPorPersonajeId(id: Int) {
        RetrofitBroker.getRequestComicsForCharacterId(
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
                    adapter.setOnItemClickListener(object : OnItemClickListener {
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
                Snackbar.make(
                    binding.drawerLayout,
                    getString(R.string.error),
                    Snackbar.LENGTH_SHORT
                ).show()
            })

    }

    private fun comprobarSiFavorito(uid: String, charact: Character?) {
        database.collection("users/$uid/characters").document(charact!!.id.toString()).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.iconFav.setImageResource(R.drawable.ic_fav_added)
                    binding.iconFav.tag = getString(R.string.fav)
                } else {
                    binding.iconFav.setImageResource(R.drawable.ic_fav_noadded)
                    binding.iconFav.tag = getString(R.string.nofav)
                }
            }
    }
}
