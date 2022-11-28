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
import quesadoprado.saramaria.marvelmania.activities.showInfo.InfoCompleteSeries
import quesadoprado.saramaria.marvelmania.adapter.SeriesAdapter
import quesadoprado.saramaria.marvelmania.data.series.Serie
import quesadoprado.saramaria.marvelmania.data.series.SeriesDTO
import quesadoprado.saramaria.marvelmania.databinding.FragmentSeriesBinding
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.interfaces.OnItemLongClickListener
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase
import quesadoprado.saramaria.marvelmania.utils.UtilsApp

class SeriesFragment(
    private val auth: FirebaseAuth,
    private val imageUser: ImageView,
    private val username: String
) : Fragment() {

    private var _binding: FragmentSeriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var series: Array<Serie>
    private val database = firebaseDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Mostramos la imagen del usuario en el Navigation Drawer
        if (auth.currentUser!=null){
            UtilsApp.mostrarImagenUser(auth.currentUser!!.uid,null,imageUser,requireContext())
        }else{
            UtilsApp.mostrarImagenUser(getString(R.string.defaultImage),null,imageUser,requireContext())
        }

        if (resources.getBoolean(R.bool.isTablet)) {
            binding.recyclerViewSeries.layoutManager = GridLayoutManager(context, 5)
        }else{
            binding.recyclerViewSeries.layoutManager = GridLayoutManager(context, 3)
        }

        //mostrar todas las series
        buscarTodasLasSeries()

        //el usuario busca por titulo la serie
        binding.ETBuscadorSerie.doOnTextChanged { _, _, _, _ ->
            if (binding.ETBuscadorSerie.text.trim().toString().isNotEmpty()) {
                buscarSeriesPorTitulo()
            } else {
                buscarTodasLasSeries()
                binding.recyclerViewSeries.visibility = View.VISIBLE
                binding.noInformationFound.visibility = View.GONE
            }
        }

    }
    //al activarse la pantalla se refrescan las series
    override fun onStart() {
        super.onStart()
        buscarTodasLasSeries()
    }
    //ocultar la barra de progreso cuando se carguen las series
    private fun ocultarProgressBar() {
        val handler = Handler()
        val runnable = Runnable {
            binding.progressbar.visibility = View.GONE
        }
        handler.postDelayed(runnable, 200)
    }


    private fun buscarTodasLasSeries() {
        //hacemos una llamada a la api para obtener todas las series
        RetrofitBroker.getRequestAllSeries(
            onResponse = {
                val respuesta: SeriesDTO = Gson().fromJson(it, SeriesDTO::class.java)

                series = respuesta.data?.results!!

                val adapter = SeriesAdapter(series)
                binding.recyclerViewSeries.adapter = adapter

                //ocultamos el progressbar
                ocultarProgressBar()

                adapter.setOnItemClickListener(object : OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val serie = series[position]
                        //nos vamos al activity donde muestra toda la información de la serie
                        val intent = Intent(context, InfoCompleteSeries::class.java)
                        intent.putExtra("serie", serie)
                        intent.putExtra("username", username)
                        startActivity(intent)
                    }
                })
                adapter.setOnItemLongClickListener(object : OnItemLongClickListener {
                    override fun onItemLongClick(position: Int, view: View): Boolean {
                        val serie = series[position]
                        /*creamos un popmenu para que el usuario pueda agregar a
                         favoritos desde la lista de series
                         */
                        val popupMenu = PopupMenu(context, view)
                        popupMenu.inflate(R.menu.menu_add_delete_fav)
                        popupMenu.setOnMenuItemClickListener { task ->
                            when (task.title) {
                                getString(R.string.addFav) -> {
                                    if (auth.currentUser != null) {
                                        //comprobamos si ya se encuentra en favoritos
                                        database.collection("users/${auth.currentUser!!.uid}/series")
                                            .document(serie.id.toString()).get()
                                            .addOnCompleteListener { seri ->
                                                if (seri.isSuccessful) {
                                                    //esta en fav
                                                    if (seri.result.exists()) {
                                                        //le comunicamos al usuario que ya esta en favoritos
                                                        Snackbar.make(
                                                            view,
                                                            getString(R.string.cantAddFav),
                                                            Snackbar.LENGTH_SHORT
                                                        ).show()
                                                        //No está en fav
                                                    } else {
                                                        DataBaseUtils.guardarSerie(
                                                            auth.currentUser!!.uid,
                                                            serie
                                                        )
                                                        buscarTodasLasSeries()
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
                                        //comprobamos si está en favoritos
                                        database.collection("users/${auth.currentUser!!.uid}/series")
                                            .document(serie.id.toString()).get()
                                            .addOnCompleteListener { seri ->
                                                if (seri.isSuccessful) {
                                                    //esta en fav
                                                    if (seri.result.exists()) {
                                                        //eliminamos la serie
                                                        DataBaseUtils.eliminarSerie(
                                                            auth.currentUser!!.uid,
                                                            serie
                                                        )
                                                        //refrescamos la lista
                                                        buscarTodasLasSeries()
                                                        //informamos al usuario
                                                        Snackbar.make(
                                                            view,
                                                            getString(R.string.removingFav),
                                                            Snackbar.LENGTH_SHORT
                                                        ).show()
                                                        //no esta en fav
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
                        //mostramos el popup
                        popupMenu.show()
                        return true
                    }
                })
            }, onFailure = {
                Snackbar.make(
                    binding.contentSerie,
                    getString(R.string.error),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun buscarSeriesPorTitulo() {
        //hacemos una llamada a la api para obtener las series que empiecen por lo que indica el usuario
        val tituloSerie = binding.ETBuscadorSerie.text.toString()
        RetrofitBroker.getRequestSerieByName(tituloSerie,
            onResponse = {
                val respuesta: SeriesDTO = Gson().fromJson(it, SeriesDTO::class.java)

                series = respuesta.data?.results!!

                if (series.size != 0) {
                    binding.recyclerViewSeries.visibility = View.VISIBLE
                    binding.noInformationFound.visibility = View.GONE

                    val adapter = SeriesAdapter(series)
                    binding.recyclerViewSeries.adapter = adapter

                    adapter.setOnItemClickListener(object : OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val serie = series[position]
                            val intent = Intent(context, InfoCompleteSeries::class.java)
                            intent.putExtra("serie", serie)
                            startActivity(intent)
                        }
                    })
                    adapter.setOnItemLongClickListener(object : OnItemLongClickListener {
                        override fun onItemLongClick(position: Int, view: View): Boolean {
                            val serie = series[position]
                            //creamos un popmenu para que el usuario pueda agregar a
                            // favoritos desde la lista de series
                            val popupMenu = PopupMenu(context, view)
                            popupMenu.inflate(R.menu.menu_add_delete_fav)
                            popupMenu.setOnMenuItemClickListener { task ->
                                when (task.title) {
                                    getString(R.string.addFav) -> {
                                        if (auth.currentUser != null) {
                                            //comprobamos si ya se encuentra en favoritos
                                            database.collection("users/${auth.currentUser!!.uid}/series")
                                                .document(serie.id.toString()).get()
                                                .addOnCompleteListener { seri ->
                                                    if (seri.isSuccessful) {
                                                        //esta en fav
                                                        if (seri.result.exists()) {
                                                            //le comunicamos al usuario que ya esta en favoritos
                                                            Snackbar.make(
                                                                view,
                                                                getString(R.string.cantAddFav),
                                                                Snackbar.LENGTH_SHORT
                                                            ).show()
                                                            //No está en fav
                                                        } else {
                                                            DataBaseUtils.guardarSerie(
                                                                auth.currentUser!!.uid,
                                                                serie
                                                            )
                                                            buscarSeriesPorTitulo()
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
                                            //comprobamos si está en favoritos
                                            database.collection("users/${auth.currentUser!!.uid}/series")
                                                .document(serie.id.toString()).get()
                                                .addOnCompleteListener { seri ->
                                                    if (seri.isSuccessful) {
                                                        //esta en fav
                                                        if (seri.result.exists()) {
                                                            //eliminamos la serie
                                                            DataBaseUtils.eliminarSerie(
                                                                auth.currentUser!!.uid,
                                                                serie
                                                            )
                                                            //refrescamos la lista
                                                            buscarSeriesPorTitulo()
                                                            //informamos al usuario
                                                            Snackbar.make(
                                                                view,
                                                                getString(R.string.removingFav),
                                                                Snackbar.LENGTH_SHORT
                                                            ).show()
                                                            //no esta en fav
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
                    //en caso de que no halla coincidencias mostramos al usuario un mensaje
                    binding.recyclerViewSeries.visibility = View.GONE
                    binding.noInformationFound.visibility = View.VISIBLE
                    binding.noInformationFound.text = getString(R.string.informacionNoEncontrada)
                }

            }, onFailure = {
                Snackbar.make(
                    binding.contentSerie,
                    getString(R.string.error),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        )
    }

}