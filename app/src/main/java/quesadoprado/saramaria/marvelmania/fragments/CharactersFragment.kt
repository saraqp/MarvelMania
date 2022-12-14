package quesadoprado.saramaria.marvelmania.fragments

import android.annotation.SuppressLint
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
import quesadoprado.saramaria.marvelmania.activities.showInfo.InfoCompleteCharacts
import quesadoprado.saramaria.marvelmania.adapter.CharacterAdapter
import quesadoprado.saramaria.marvelmania.data.characters.Character
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.databinding.FragmentCharactersBinding
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.interfaces.OnItemLongClickListener
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase
import quesadoprado.saramaria.marvelmania.utils.UtilsApp

class CharactersFragment(private val auth: FirebaseAuth, private val imageUser: ImageView) :
    Fragment() {

    private var _binding: FragmentCharactersBinding? = null
    private val binding get() = _binding!!
    private lateinit var characters: Array<Character>
    private val database = firebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharactersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*Si el usuario est?? desde una tablet se cambia visualmente el numero de columnas de la vista
         * para una visualizacion mas comoda
         */
        if (resources.getBoolean(R.bool.isTablet)) {
            binding.recyclerViewCharacters.layoutManager = GridLayoutManager(context, 5)
        } else {
            binding.recyclerViewCharacters.layoutManager = GridLayoutManager(context, 3)
        }

        if (auth.currentUser != null) {
            UtilsApp.mostrarImagenUser(auth.currentUser!!.uid, null, imageUser, requireContext())
        } else {
            UtilsApp.mostrarImagenUser(
                getString(R.string.defaultImage),
                null,
                imageUser,
                requireContext()
            )
        }

        //Mostrar todos los personajes
        buscarTodoslosPersonajes()

        //cuando el usuario buscar por nombre
        binding.ETBuscadorChar.doOnTextChanged { _, _, _, _ ->
            if (binding.ETBuscadorChar.text.trim().toString().isNotEmpty()) {
                buscarPersonajePorNombre()
            } else {
                buscarTodoslosPersonajes()
                binding.noInformationFound.visibility = View.GONE
                binding.recyclerViewCharacters.visibility = View.VISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (binding.ETBuscadorChar.text.isEmpty()) {
            buscarTodoslosPersonajes()
        } else {
            buscarPersonajePorNombre()
        }
    }

    //Buscamos todos los personajes
    private fun buscarTodoslosPersonajes() {
        RetrofitBroker.getRequestAllCharacters(
            onResponse = {
                val respuesta = Gson().fromJson(it, CharactersDTO::class.java)
                characters = respuesta?.data?.results!!

                val adapter = CharacterAdapter(characters)
                binding.recyclerViewCharacters.adapter = adapter
                ocultarProgressBar()
                adapter.setOnItemClickListener(object : OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        binding.ETBuscadorChar.setText("")
                        binding.ETBuscadorChar.invalidate();
                        val character = characters[position]
                        val intent = Intent(context, InfoCompleteCharacts::class.java)
                        intent.putExtra("charact", character)
                        startActivity(intent)
                    }
                })

                adapter.setOnItemLongClickListener(object : OnItemLongClickListener {
                    @SuppressLint("ResourceAsColor")
                    override fun onItemLongClick(position: Int, view: View): Boolean {
                        val character = characters[position]
                        //creamos un popup para que pueda agregar o eliminar personaje
                        val popupMenu = PopupMenu(context, view)
                        popupMenu.inflate(R.menu.menu_add_delete_fav)
                        popupMenu.setOnMenuItemClickListener { task ->
                            when (task.title) {
                                getString(R.string.addFav) -> {
                                    if (auth.currentUser != null) {
                                        //comprobamos si el personaje seleccionado ya est?? en favoritos
                                        database.collection("users/${auth.currentUser!!.uid}/characters")
                                            .document(character.id.toString()).get()
                                            .addOnCompleteListener { charact ->
                                                if (charact.isSuccessful) {
                                                    //est?? en favoritos
                                                    if (charact.result.exists()) {
                                                        //comunicamos al usuario que ya pertenede a favoritos
                                                        Snackbar.make(
                                                            view,
                                                            getString(R.string.cantAddFav),
                                                            Snackbar.LENGTH_SHORT
                                                        ).show()
                                                        //no est?? en favoritos
                                                    } else {
                                                        //guardamos en la base de datos la informaci??n del personaje y se lo comunicamos al usuario
                                                        DataBaseUtils.guardarPersonaje(
                                                            auth.currentUser!!.uid,
                                                            character
                                                        )
                                                        buscarTodoslosPersonajes()
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
                                        //comprobamos si el personaje esta como favorito
                                        database.collection("users/${auth.currentUser!!.uid}/characters")
                                            .document(character.id.toString()).get()
                                            .addOnCompleteListener { charact ->
                                                if (charact.isSuccessful) {
                                                    //est?? en favoritos
                                                    if (charact.result.exists()) {
                                                        //eliminamos al personaje de la base de datos de favoritos del usuario y le informamos
                                                        DataBaseUtils.eliminarPersonaje(
                                                            auth.currentUser!!.uid,
                                                            character
                                                        )
                                                        buscarTodoslosPersonajes()
                                                        Snackbar.make(
                                                            view,
                                                            getString(R.string.removingFav),
                                                            Snackbar.LENGTH_SHORT
                                                        ).show()

                                                        //no est?? en favoritos
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
                    binding.contentCharacters,
                    getString(R.string.error),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        )
    }

    //Ocultamos el progressBar despues de 0,2 segundos
    private fun ocultarProgressBar() {
        val handler = Handler()
        val runnable = Runnable {
            binding.progressbar.visibility = View.GONE
        }
        handler.postDelayed(runnable, 200)
    }

    //Buscamos la lista de personajes por el nombre indicado por el usuario
    private fun buscarPersonajePorNombre() {
        val nombrePersonaje = binding.ETBuscadorChar.text.toString()
        RetrofitBroker.getRequestCharactersByName(nombrePersonaje,
            onResponse = {
                val respuesta = Gson().fromJson(it, CharactersDTO::class.java)
                characters = respuesta?.data?.results!!

                if (characters.isNotEmpty()) {
                    binding.noInformationFound.visibility = View.GONE
                    binding.recyclerViewCharacters.visibility = View.VISIBLE

                    val adapter = CharacterAdapter(characters)
                    binding.recyclerViewCharacters.adapter = adapter

                    adapter.setOnItemClickListener(object : OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val character = characters[position]
                            val intent = Intent(context, InfoCompleteCharacts::class.java)
                            intent.putExtra("charact", character)
                            startActivity(intent)
                        }
                    })
                    adapter.setOnItemLongClickListener(object : OnItemLongClickListener {
                        @SuppressLint("ResourceAsColor")
                        override fun onItemLongClick(position: Int, view: View): Boolean {
                            val character = characters[position]
                            //Mostramos un popup para poder a??adir/eliminar de favoritos desde la lista de personajes
                            val popupMenu = PopupMenu(
                                context,
                                view
                            )
                            popupMenu.inflate(R.menu.menu_add_delete_fav)
                            popupMenu.setOnMenuItemClickListener { task ->
                                when (task.title) {
                                    getString(R.string.addFav) -> {
                                        if (auth.currentUser != null) {
                                            //guardamos en la base de datos el id del personaje
                                            // y que es un personaje para poder buscar su informaci??n en la api
                                            DataBaseUtils.guardarPersonaje(
                                                auth.currentUser!!.uid,
                                                character
                                            )
                                            buscarPersonajePorNombre()
                                            Snackbar.make(
                                                view,
                                                "Personaje a??adido a favoritos",
                                                Snackbar.LENGTH_SHORT
                                            ).show()
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
                                        DataBaseUtils.eliminarPersonaje(
                                            auth.currentUser!!.uid,
                                            character
                                        )
                                        buscarPersonajePorNombre()
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
                    binding.recyclerViewCharacters.visibility = View.GONE
                    binding.noInformationFound.visibility = View.VISIBLE
                    binding.noInformationFound.text = getString(R.string.informacionNoEncontrada)
                }

            }, onFailure = {
                Snackbar.make(
                    binding.contentCharacters,
                    getString(R.string.error),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        )
    }

}