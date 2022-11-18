package quesadoprado.saramaria.marvelmania.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import quesadoprado.saramaria.marvelmania.activities.showInfo.InfoCompleteSeries
import quesadoprado.saramaria.marvelmania.adapter.SeriesFavouritesAdapter
import quesadoprado.saramaria.marvelmania.data.items.Item
import quesadoprado.saramaria.marvelmania.data.items.Thumbnail
import quesadoprado.saramaria.marvelmania.data.series.Serie
import quesadoprado.saramaria.marvelmania.databinding.FragmentListFavoritesSeriesBinding
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils

class ListFavoritesSeriesFragment : Fragment() {
    private var _binding:FragmentListFavoritesSeriesBinding?=null
    private val binding get()=_binding!!
    private var database= FirebaseUtils.firebaseDatabase
    private val currentUser= FirebaseUtils.firebaseAuth.currentUser
    private lateinit var rutaColeccionSerieFavoritos:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentListFavoritesSeriesBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (currentUser!=null){
            rutaColeccionSerieFavoritos="users/${currentUser.uid}/series"
            obtenerSeriesFavoritos(rutaColeccionSerieFavoritos)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (currentUser!=null){
            rutaColeccionSerieFavoritos="users/${currentUser.uid}/series"
            obtenerSeriesFavoritos(rutaColeccionSerieFavoritos)
        }
    }
    private fun obtenerSeriesFavoritos(ruta: String) {
        database.collection(ruta).get()
            .addOnCompleteListener { document->
                if (document.isSuccessful){
                    var series= arrayOf<Serie>()
                    for(i in 0 until document.result.size()){
                        //datos de las series
                        val item=document.result.documents[i].data
                        val id=item!!["id"] as Long?
                        val description=item["description"] as String?
                        val endYear=item["endYear"] as Long?
                        val rating=item["rating"] as String?
                        val startYear=item["startYear"] as Long?
                        val thumbnailHashMap=item["thumbnail"] as HashMap<*,*>
                        val thumbnail=Thumbnail(
                            thumbnailHashMap["path"] as String?,
                            thumbnailHashMap["extension"] as String?
                        )
                        val title=item["title"] as String?
                        val nextHashMap=item["next"] as Any?
                        val next:Item? = if (nextHashMap is HashMap<*,*>){
                            Item(
                                nextHashMap["name"] as String?,
                                nextHashMap["resourceURI"] as String?
                            )
                        }else{
                            null
                        }
                        val previousHashMap=item["previous"] as Any?
                        val previous:Item?=if (previousHashMap is HashMap<*,*>){
                            Item(
                                previousHashMap["name"] as String?,
                                previousHashMap["resourceURI"] as String?
                            )
                        }else{
                            null
                        }
                        val serie=Serie(id!!.toInt(),title,description,startYear?.toInt(), endYear?.toInt(), rating, thumbnail,next,previous)
                        series=series.plus(serie)

                    }
                    //mostramos las series
                    binding.listaSeriesFavoritos.layoutManager=GridLayoutManager(context,2)
                    val adapter=SeriesFavouritesAdapter(series)
                    binding.listaSeriesFavoritos.adapter=adapter
                    adapter.setOnItemClickListener(object : OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            val serie=series[position]
                            val intent= Intent(context, InfoCompleteSeries::class.java)
                            intent.putExtra("serie",serie)
                            startActivity(intent)
                        }

                    })
                }
            }
    }


}