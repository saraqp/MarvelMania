package quesadoprado.saramaria.marvelmania.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import quesadoprado.saramaria.marvelmania.activities.showInfo.InfoCompleteCharacts
import quesadoprado.saramaria.marvelmania.adapter.CharacterFavouritesAdapter
import quesadoprado.saramaria.marvelmania.data.characters.Character
import quesadoprado.saramaria.marvelmania.data.items.Thumbnail
import quesadoprado.saramaria.marvelmania.databinding.FragmentListFavoritesCharactersBinding
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils

class ListFavoritesCharactersFragment : Fragment() {
    private var _binding: FragmentListFavoritesCharactersBinding?=null
    private val binding get() = _binding!!

    private var database= FirebaseUtils.firebaseDatabase
    private val currentUser=FirebaseUtils.firebaseAuth.currentUser
    private lateinit var rutaColeccionCharactersFavoritos:String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding=FragmentListFavoritesCharactersBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onStart() {
        super.onStart()
        if (currentUser!=null) {
            rutaColeccionCharactersFavoritos="users/${currentUser.uid}/characters"
            obtenerPersonajesFavoritos(rutaColeccionCharactersFavoritos)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (currentUser!=null) {
            rutaColeccionCharactersFavoritos = "users/" + currentUser.uid + "/characters"
            obtenerPersonajesFavoritos(rutaColeccionCharactersFavoritos)
        }


    }
    private fun obtenerPersonajesFavoritos(ruta:String){
        database.collection(ruta).get()
            .addOnCompleteListener{ document->
                if (document.isSuccessful){
                    //obtenemos los personajes
                    var personajes= arrayOf<Character>()
                    for (i in 0 until document.result.size()) {
                        val item=document.result.documents[i].data
                        val id= item!!["id"] as Long?
                        val name= item["name"] as String?
                        val description= item["description"] as String?
                        val thumbnailhashMap= item["thumbnail"] as HashMap<*, *>
                        val thumbnail= Thumbnail(
                            thumbnailhashMap["path"] as String,
                            thumbnailhashMap["extension"] as String
                        )
                        val character=Character(id?.toInt(),name,description,thumbnail)
                        personajes=personajes.plus(character)
                    }
                    //Mostramos los personajes favoritos del usuario
                    binding.listaPersonajesFavoritos.layoutManager= GridLayoutManager(context,2)
                    binding.listaPersonajesFavoritos.setHasFixedSize(true)
                    val adapter= CharacterFavouritesAdapter(personajes)
                    binding.listaPersonajesFavoritos.adapter=adapter
                    adapter.setOnItemClickListener(object : OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val character= personajes[position]
                            val intent= Intent(context, InfoCompleteCharacts::class.java)
                            intent.putExtra("charact",character)
                            startActivity(intent)
                        }
                    })
                }
            }
    }

}