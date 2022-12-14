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
import quesadoprado.saramaria.marvelmania.adapter.ListCharactersAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListVariantImagesComicsAdapter
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.data.util.Coment
import quesadoprado.saramaria.marvelmania.databinding.ActivityInfocompletecomicsBinding
import quesadoprado.saramaria.marvelmania.interfaces.OnComentClickListener
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
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

        //a??adir boton para volver a la biblioteca con el icono personalizado
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
        //en caso de que ambas descripciones est??n vac??as mostramos que no se ha encontrado ninguna descripci??n
        if (comic?.description.equals("") && comic?.variantDescription.equals("")) {
            binding.descripcionText.text = getString(R.string.noHayDescripcion)

            //si solo descripci??n esta vac??a mostramos la descripci??n alternativa
        } else if (comic?.description.equals("")) {
            binding.descripcionText.text = comic?.variantDescription

        } else {
            binding.descripcionText.text = comic?.description
        }
        //mostramos el numero y p??ginas y formato que tiene el comic
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

        //comprobamos si el usuario esta logueado, en caso correcto muestra la secci??n de comentarios
        if (auth.currentUser != null) {
            binding.contentComentarios.visibility = View.VISIBLE

            binding.listaComentarios.layoutManager = LinearLayoutManager(context)
            updateComentsUI(comic.id)
            binding.btnComent.setOnClickListener {
                obtenerNombreUsuario(comic.id)
                hideKeyboard()
                binding.respuestaComent.visibility = View.GONE
                binding.escribirComentario.text = null
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
    /*cada minuto actualizamos la lista de comentarios para que el usuario la tenga actualizada si
    *permanece mucho tiempo en la misma vista
    * */
    private fun updateComentsUI(id: Int) {
        runnable = Runnable {
            obtenerComentarios(id)
            handler.postDelayed(runnable, 60000)
        }
        handler.post(runnable)
    }

    //obtenemos el nombre de usuario y uid del que escribe el mensaje y guardamos el comentario
    private fun obtenerNombreUsuario(id: Int) {
        val comentario_user = binding.escribirComentario.text.toString()
        database.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { task ->
                if (task.exists()) {
                    val username = task.data!!["displayName"] as String
                    coment = Coment(
                        "comic",
                        id,
                        username,
                        auth.currentUser!!.uid,
                        0,
                        comentario_user,
                        idComentResp,
                        coment?.idComent,
                        false
                    )
                    if (comentario_user.trim().isNotEmpty()) {
                        DataBaseUtils.guardarComentario(coment!!)
                        idComentResp = null
                        obtenerComentarios(id)
                    }
                }
            }
    }

    //despues de 0,2 segundos se oculta el ProgressBar
    private fun ocultarProgressBar() {
        val handler = Handler()
        val runnable = Runnable {
            binding.progressbarCharacters.visibility = View.GONE
        }
        handler.postDelayed(runnable, 200)
    }

    private fun obtenerComentarios(id: Int) {
        database.collection("coments").get().addOnSuccessListener { document ->
            val comentarios = document.documents
            var lista_coments = arrayOf<Coment>()
            for (coment in comentarios) {
                if (coment.data!!["type"] == "comic") {
                    val id_type = (coment.data!!["id_type"] as Long).toInt()
                    //comprobamos q el comentario corresponda con el comic visualizado
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
                        lista_coments = lista_coments.plus(comentario)
                        lista_coments.sortByDescending { it.puntuacion }
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

                                    lista_coments[position] = coment
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

                                        lista_coments[position] = coment
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

                                        lista_coments[position] = coment
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

                                    lista_coments[position] = coment
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

                                        lista_coments[position] = coment
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

                                        lista_coments[position] = coment
                                        adapter.notifyDataSetChanged()
                                    }

                            }
                        }
                    }
                }

                override fun onDeleteClick(position: Int) {
                    val comentario = lista_coments[position]
                    //verificamos que el usuario est?? seguro de borrar su comentario
                    val builder = AlertDialog.Builder(context)

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

                            lista_coments[position] = comentario
                            adapter.notifyDataSetChanged()
                        }
                        //en caso negativo no hacemos nada
                        .setNegativeButton(getString(R.string.no)) { _, _ -> }
                        .show()
                }

                override fun onEditClick(position: Int) {
                    val coment = lista_coments[position]
                    //mostramos un dialog para que el usuario pueda editar el mensaje
                    val dialogEditComent: AlertDialog.Builder = AlertDialog.Builder(context)
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

                                lista_coments[position] = coment
                                adapter.notifyDataSetChanged()

                            } else {
                                Snackbar.make(
                                    binding.drawerLayout,
                                    getString(R.string.campoNoVacio),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }

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

    //obtenemos la lista de personajes del comics que estemos visualizando
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
                    adapter.setOnItemClickListener(object : OnItemClickListener {
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
                Snackbar.make(
                    binding.drawerLayout,
                    getString(R.string.error),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        )
    }

    //comprobamos si est?? en favoritos para mostrarlo visualmente al usuario
    private fun comprobarSiFavorito(uid: String, comic: Comic?) {
        database.collection("users/$uid/comics").document(comic!!.id.toString()).get()
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