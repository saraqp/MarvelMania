package quesadoprado.saramaria.marvelmania.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.activities.showInfo.InfoCompleteSeries
import quesadoprado.saramaria.marvelmania.adapter.SeriesAdapter
import quesadoprado.saramaria.marvelmania.data.series.Serie
import quesadoprado.saramaria.marvelmania.data.series.SeriesDTO
import quesadoprado.saramaria.marvelmania.databinding.FragmentSeriesBinding
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker

class SeriesFragment(auth: FirebaseAuth) : Fragment() {

    private var _binding: FragmentSeriesBinding?=null
    private val binding get() = _binding!!
    private lateinit var series:Array<Serie>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSeriesBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewSeries.layoutManager=GridLayoutManager(context,2)
        buscarTodasLasSeries()
        binding.BbuscarSerie.setOnClickListener {
            if(binding.ETBuscadorSerie.text.trim().toString().isNotEmpty()){
                var tituloSerie=binding.ETBuscadorSerie.text.toString()
                RetrofitBroker.getRequestSerieByName(tituloSerie,
                    onResponse = {
                        var respuesta: SeriesDTO =Gson().fromJson(it, SeriesDTO::class.java)

                        series=respuesta.data?.results!!
                        val adapter= SeriesAdapter(series)
                        binding.recyclerViewSeries.adapter=adapter

                        adapter.setOnItemClickListener(object : SeriesAdapter.onIntemClickListener{
                            override fun onItemClick(position: Int) {
                                val serie=series?.get(position)
                                val intent= Intent(context, InfoCompleteSeries::class.java)
                                intent.putExtra("serie",serie)
                                startActivity(intent)
                            }
                        })
                    }, onFailure = {
                        Toast.makeText(context,getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                )
            }else{
                buscarTodasLasSeries()
            }
        }

    }

    private fun buscarTodasLasSeries(){
        RetrofitBroker.getRequestAllSeries(
            onResponse = {
                var respuesta: SeriesDTO =Gson().fromJson(it, SeriesDTO::class.java)

                series=respuesta.data?.results!!
                val adapter= SeriesAdapter(series)
                binding.recyclerViewSeries.adapter=adapter

                adapter.setOnItemClickListener(object : SeriesAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        val serie=series?.get(position)
                        val intent= Intent(context, InfoCompleteSeries::class.java)
                        intent.putExtra("serie",serie)
                        startActivity(intent)
                    }
                })
            }, onFailure = {
                Toast.makeText(context,getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        )
    }

}