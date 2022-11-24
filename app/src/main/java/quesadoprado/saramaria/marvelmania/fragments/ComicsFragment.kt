package quesadoprado.saramaria.marvelmania.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.activities.showInfo.InfoCompleteComics
import quesadoprado.saramaria.marvelmania.adapter.ComicAdapter
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.data.comics.ComicsDTO
import quesadoprado.saramaria.marvelmania.databinding.FragmentComicsBinding
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.interfaces.OnItemLongClickListener
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase
import quesadoprado.saramaria.marvelmania.utils.UtilsApp


class ComicsFragment(private val auth: FirebaseAuth, private val imageUser: ImageView) :
    Fragment() {

    private var _binding: FragmentComicsBinding? = null
    private val binding get() = _binding!!
    private lateinit var comics: Array<Comic>
    private val database = firebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (auth.currentUser!=null){
            UtilsApp.mostrarImagenUser(auth.currentUser!!.uid,null,imageUser,requireContext())
        }else{
            UtilsApp.mostrarImagenUser(getString(R.string.defaultImage),null,imageUser,requireContext())
        }
        binding.recyclerViewComics.layoutManager = GridLayoutManager(context, 3)
        //Mostrar todos los comics
        buscarTodosLosComics()
        //Cuando el usuario busca por titulo
        binding.ETBuscadorComic.doOnTextChanged { _, _, _, _ ->
            if (binding.ETBuscadorComic.text.toString().trim().isNotEmpty()) {
                buscarComicsPorTitulo()
            } else {
                buscarTodosLosComics()
                binding.noInformationFound.visibility = View.GONE
                binding.recyclerViewComics.visibility = View.VISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        buscarTodosLosComics()
    }

    private fun buscarTodosLosComics() {
        RetrofitBroker.getRequestAllComics(
            onResponse = {
                val gson = Gson().newBuilder().setDateFormat("yyyy-MM-dd").create()
                val respuesta: ComicsDTO = gson.fromJson(it, ComicsDTO::class.java)

                comics = respuesta.data?.results!!
                val adapter = ComicAdapter(comics)
                binding.recyclerViewComics.adapter = adapter
                ocultarProgressBar()
                adapter.setOnItemClickListener(object : OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val comic = comics[position]
                        val intent = Intent(context, InfoCompleteComics::class.java)
                        intent.putExtra("comic", comic)
                        startActivity(intent)
                    }

                })
                adapter.setOnItemLongClickListener(object : OnItemLongClickListener {
                    override fun onItemLongClick(position: Int, view: View): Boolean {
                        val comic = comics[position]
                        val popupMenu = PopupMenu(context, view)
                        popupMenu.inflate(R.menu.menu_add_delete_fav)
                        popupMenu.setOnMenuItemClickListener { task ->
                            when (task.title) {
                                getString(R.string.addFav) -> {
                                    if (auth.currentUser != null) {
                                        database.collection("users/${auth.currentUser!!.uid}/comics")
                                            .document(comic.id.toString()).get()
                                            .addOnCompleteListener { comics ->
                                                if (comics.isSuccessful) {
                                                    //se encuentra en favoritos
                                                    if (comics.result.exists()) {
                                                        //comunicamos q se encuentra en favoritos
                                                        Snackbar.make(
                                                            view,
                                                            getString(R.string.cantAddFav),
                                                            Snackbar.LENGTH_SHORT
                                                        ).show()
                                                        //no está en favoritos
                                                    } else {
                                                        DataBaseUtils.guardarComic(
                                                            auth.currentUser!!.uid,
                                                            comic
                                                        )
                                                        buscarTodosLosComics()
                                                        Snackbar.make(
                                                            view,
                                                            getString(R.string.addingFav),
                                                            Snackbar.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                    } else {
                                        Snackbar.make(
                                            view,
                                            getString(R.string.necesitasLogin),
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                    true
                                }
                                getString(R.string.delFav) -> {
                                    if (auth.currentUser != null) {
                                        //comprobamos si el comic esta en favoritos
                                        database.collection("users/${auth.currentUser!!.uid}/comics")
                                            .document(comic.id.toString()).get()
                                            .addOnCompleteListener { comc ->
                                                if (comc.isSuccessful) {
                                                    //se encuentra en fav
                                                    if (comc.result.exists()) {
                                                        DataBaseUtils.eliminarComic(
                                                            auth.currentUser!!.uid,
                                                            comic
                                                        )
                                                        //actualizamos la lista para que desaparezca de favoritos
                                                        buscarTodosLosComics()
                                                        Snackbar.make(
                                                            view,
                                                            getString(R.string.removingFav),
                                                            Snackbar.LENGTH_SHORT
                                                        ).show()
                                                        //No esta en fav
                                                    } else {
                                                        Snackbar.make(
                                                            view,
                                                            getString(R.string.cantRemFav),
                                                            Snackbar.LENGTH_SHORT
                                                        ).show()

                                                    }
                                                }
                                            }
                                    } else {
                                        Snackbar.make(
                                            view,
                                            getString(R.string.necesitasLogin),
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                    true
                                }
                                else -> {
                                    false
                                }
                            }
                        }
                        popupMenu.show()
                        return true
                    }

                })

            }, onFailure = {
                Snackbar.make(
                    binding.contentComics,
                    getString(R.string.error),
                    Snackbar.LENGTH_SHORT
                ).show()

            }
        )
    }

    private fun ocultarProgressBar() {
        val handler = Handler()
        val runnable = Runnable {
            binding.progressbar.visibility = View.GONE
        }
        handler.postDelayed(runnable, 200)
    }

    private fun buscarComicsPorTitulo() {
        val tituloComic = binding.ETBuscadorComic.text.toString()
        RetrofitBroker.getRequestComicByName(tituloComic,
            onResponse = {
                val gson = Gson().newBuilder().setDateFormat("yyyy-MM-dd").create()
                val respuesta: ComicsDTO = gson.fromJson(it, ComicsDTO::class.java)

                comics = respuesta.data?.results!!

                if (comics.size != 0) {
                    binding.noInformationFound.visibility = View.GONE
                    binding.recyclerViewComics.visibility = View.VISIBLE
                    val adapter = ComicAdapter(comics)
                    binding.recyclerViewComics.adapter = adapter
                    adapter.setOnItemClickListener(object : OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val comic = comics[position]
                            val intent = Intent(context, InfoCompleteComics::class.java)
                            intent.putExtra("comic", comic)
                            startActivity(intent)
                        }

                    })
                    adapter.setOnItemLongClickListener(object : OnItemLongClickListener {
                        override fun onItemLongClick(position: Int, view: View): Boolean {
                            val comic = comics[position]
                            val popupMenu = PopupMenu(context, view)
                            popupMenu.inflate(R.menu.menu_add_delete_fav)
                            popupMenu.setOnMenuItemClickListener { task ->
                                when (task.title) {
                                    getString(R.string.addFav) -> {
                                        if (auth.currentUser != null) {
                                            database.collection("users/${auth.currentUser!!.uid}/comics")
                                                .document(comic.id.toString()).get()
                                                .addOnCompleteListener { comics ->
                                                    if (comics.isSuccessful) {
                                                        //se encuentra en favoritos
                                                        if (comics.result.exists()) {
                                                            //comunicamos q se encuentra en favoritos
                                                            Snackbar.make(
                                                                view,
                                                                getString(R.string.cantAddFav),
                                                                Snackbar.LENGTH_SHORT
                                                            ).show()
                                                            //no está en favoritos
                                                        } else {
                                                            DataBaseUtils.guardarComic(
                                                                auth.currentUser!!.uid,
                                                                comic
                                                            )
                                                            buscarComicsPorTitulo()
                                                            Snackbar.make(
                                                                view,
                                                                getString(R.string.addingFav),
                                                                Snackbar.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                        } else {
                                            Snackbar.make(
                                                view,
                                                getString(R.string.necesitasLogin),
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                        true
                                    }
                                    getString(R.string.delFav) -> {
                                        if (auth.currentUser != null) {
                                            //comprobamos si el comic esta en favoritos
                                            database.collection("users/${auth.currentUser!!.uid}/comics")
                                                .document(comic.id.toString()).get()
                                                .addOnCompleteListener { comc ->
                                                    if (comc.isSuccessful) {
                                                        //se encuentra en fav
                                                        if (comc.result.exists()) {
                                                            DataBaseUtils.eliminarComic(
                                                                auth.currentUser!!.uid,
                                                                comic
                                                            )
                                                            //actualizamos la lista para que desaparezca de favoritos
                                                            buscarComicsPorTitulo()
                                                            Snackbar.make(
                                                                view,
                                                                getString(R.string.removingFav),
                                                                Snackbar.LENGTH_SHORT
                                                            ).show()
                                                            //No esta en fav
                                                        } else {
                                                            Snackbar.make(
                                                                view,
                                                                getString(R.string.cantRemFav),
                                                                Snackbar.LENGTH_SHORT
                                                            ).show()

                                                        }
                                                    }
                                                }
                                        } else {
                                            Snackbar.make(
                                                view,
                                                getString(R.string.necesitasLogin),
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                        true
                                    }
                                    else -> {
                                        false
                                    }
                                }
                            }
                            popupMenu.show()
                            return true
                        }

                    })
                } else {
                    binding.noInformationFound.visibility = View.VISIBLE
                    binding.recyclerViewComics.visibility = View.GONE
                    binding.noInformationFound.text = getString(R.string.informacionNoEncontrada)
                }

            }, onFailure = {
                Snackbar.make(
                    binding.contentComics,
                    getString(R.string.error),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        )

    }
}