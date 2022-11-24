package quesadoprado.saramaria.marvelmania.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import quesadoprado.saramaria.marvelmania.activities.showInfo.InfoCompleteComics
import quesadoprado.saramaria.marvelmania.adapter.ComicFavouritesAdapter
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.data.items.Thumbnail
import quesadoprado.saramaria.marvelmania.databinding.FragmentListFavoritesComicsBinding
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils

class ListFavoritesComicsFragment : Fragment() {
    private var _binding: FragmentListFavoritesComicsBinding? = null
    private val binding get() = _binding!!

    private var database = FirebaseUtils.firebaseDatabase
    private val currentUser = FirebaseUtils.firebaseAuth.currentUser
    private lateinit var rutaColeccionComicsFavoritos: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListFavoritesComicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (currentUser != null) {
            rutaColeccionComicsFavoritos = "users/${currentUser.uid}/comics"
            obtenerComicsFavoritos(rutaColeccionComicsFavoritos)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (currentUser != null) {
            rutaColeccionComicsFavoritos = "users/${currentUser.uid}/comics"
            obtenerComicsFavoritos(rutaColeccionComicsFavoritos)
        }
    }

    private fun obtenerComicsFavoritos(ruta: String) {
        database.collection(ruta).get()
            .addOnCompleteListener { document ->
                if (document.isSuccessful) {
                    //obtenemos los comics
                    var comics = arrayOf<Comic>()
                    for (i in 0 until document.result.size()) {
                        //datos de los comics
                        val item = document.result.documents[i].data
                        val id = item!!["id"] as Long?
                        val description = item["description"] as String?
                        val format = item["format"] as String?
                        val pageCount = item["pageCount"] as Long?
                        val title = item["title"] as String?
                        val variantDescription = item["vatiantDescription"] as String?
                        //variantImages
                        val imagesList = item["images"] as List<HashMap<*, *>>
                        var images = arrayOf<Thumbnail>()
                        //imagelist devuelve una List<Map<String,String>>
                        for (img in imagesList) {
                            images = images.plus(
                                Thumbnail(
                                    img["path"] as String,
                                    img["extension"] as String
                                )
                            )
                        }
                        val thumbnailMap = item["thumbnail"] as HashMap<*, *>
                        val thumbnail = Thumbnail(
                            thumbnailMap["path"] as String,
                            thumbnailMap["extension"] as String
                        )
                        val comic = Comic(
                            id!!.toInt(),
                            title,
                            variantDescription,
                            description,
                            format,
                            pageCount!!.toInt(),
                            thumbnail,
                            images
                        )
                        comics = comics.plus(comic)
                    }
                    //mostramos los comics

                    binding.listaComicsFavoritos.layoutManager = GridLayoutManager(context, 3)
                    val adapter = ComicFavouritesAdapter(comics)
                    binding.listaComicsFavoritos.adapter = adapter
                    adapter.setOnItemClickListener(object : OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val comic = comics[position]
                            val intent = Intent(context, InfoCompleteComics::class.java)
                            intent.putExtra("comic", comic)
                            startActivity(intent)
                        }
                    })
                }
            }
    }

}