package quesadoprado.saramaria.marvelmania.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.activities.showInfo.InfoCompleteCharacts
import quesadoprado.saramaria.marvelmania.adapter.CharacterAdapter
import quesadoprado.saramaria.marvelmania.data.characters.Character
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.databinding.FragmentCharactersBinding
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class CharactersFragment(private val auth: FirebaseAuth) : Fragment() {

    private var _binding:FragmentCharactersBinding?=null
    private val binding get() = _binding!!
    private lateinit var characters:Array<Character>
    private val database=firebaseDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding=FragmentCharactersBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewCharacters.layoutManager=GridLayoutManager(context,2)

        //Mostrar todos los personajes
        buscarTodoslosPersonajes()
        binding.BbuscarCharact.setOnClickListener {
            if(binding.ETBuscadorChar.text.trim().toString().isNotEmpty()){
                buscarPersonajePorNombre()
            }else{
                buscarTodoslosPersonajes()
            }
        }
    }
    private fun buscarTodoslosPersonajes() {
        RetrofitBroker.getRequestAllCharacters(
            onResponse = {
                val respuesta=Gson().fromJson(it, CharactersDTO::class.java)
                characters= respuesta?.data?.results!!

                val adapter=CharacterAdapter(characters)
                binding.recyclerViewCharacters.adapter=adapter

                adapter.setOnItemClickListener(object : CharacterAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        val character= characters[position]
                        val intent=Intent(context,InfoCompleteCharacts::class.java)
                        intent.putExtra("charact",character)
                        startActivity(intent)
                    }
                })
                adapter.setOnItemLongClickListener(object :CharacterAdapter.onIntemLongClickListener{
                    @SuppressLint("ResourceAsColor")
                    override fun onItemLongClick(position: Int, view: View): Boolean {
                        val character=characters[position]
                        val popupMenu=PopupMenu(
                            context,
                            view
                        )
                        popupMenu.inflate(R.menu.menu_add_delete_fav)
                        popupMenu.setOnMenuItemClickListener { it ->
                            when(it.title){
                                getString(R.string.addFav)->{
                                    if (auth.currentUser!=null){
                                        //guardamos en la base de datos el id del personaje
                                        // y que es un personaje para poder buscar su información en la api
                                        database.collection("users").document(auth.currentUser!!.email.toString())
                                            .collection("favourites").document(character.id.toString()).set(
                                                hashMapOf(
                                                    "id" to character.id,
                                                    "type" to "character"
                                                )
                                            )
                                    }else{
                                        Snackbar.make(view,getString(R.string.necesitasLogin),Snackbar.LENGTH_SHORT).show()
                                    }
                                    true
                                }
                                getString(R.string.delFav)->{
                                    Toast.makeText(context,"clock on del",Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context,getString(R.string.error),Toast.LENGTH_SHORT).show()

            }
        )
    }
    private fun buscarPersonajePorNombre(){
        val nombrePersonaje=binding.ETBuscadorChar.text.toString()
        RetrofitBroker.getRequestCharactersByName(nombrePersonaje,
            onResponse = {
                val respuesta=Gson().fromJson(it, CharactersDTO::class.java)
                characters= respuesta?.data?.results!!

                val adapter=CharacterAdapter(characters)
                binding.recyclerViewCharacters.adapter=adapter

                adapter.setOnItemClickListener(object : CharacterAdapter.onIntemClickListener {
                    override fun onItemClick(position: Int) {
                        val character = characters[position]
                        val intent = Intent(context, InfoCompleteCharacts::class.java)
                        intent.putExtra("charact", character)
                        startActivity(intent)
                    }
                })
                adapter.setOnItemLongClickListener(object :CharacterAdapter.onIntemLongClickListener{
                    override fun onItemLongClick(position: Int, view: View): Boolean {
                        val character=characters[position]
                        val popupMenu=PopupMenu(
                            context,
                            view
                        )
                        popupMenu.inflate(R.menu.menu_add_delete_fav)
                        popupMenu.show()
                        return true
                    }

                })
            }, onFailure = {
                Toast.makeText(context,getString(R.string.error),Toast.LENGTH_SHORT).show()
            }
        )
    }

}