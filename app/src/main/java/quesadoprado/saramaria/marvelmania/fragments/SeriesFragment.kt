package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.adapter.SeriesAdapter
import quesadoprado.saramaria.marvelmania.data.comics.ComicsDTO
import quesadoprado.saramaria.marvelmania.data.series.SeriesDTO
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker

class SeriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_series, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView:RecyclerView=view.findViewById(R.id.recyclerViewSeries)
        recyclerView.layoutManager=GridLayoutManager(context,2)

        RetrofitBroker.getRequestAllSeries(
            onResponse = {
                var gson=Gson().newBuilder().setDateFormat("yyyy-MM-dd").create()
                var respuesta: SeriesDTO =gson.fromJson(it, SeriesDTO::class.java)

                val series=respuesta.data?.results
                val adapter= SeriesAdapter(series)
                recyclerView.adapter=adapter

                adapter.setOnItemClickListener(object : SeriesAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        Toast.makeText(context,series?.get(position).toString(), Toast.LENGTH_SHORT).show()
                    }
                })
            }, onFailure = {
                Toast.makeText(context,getString(R.string.error), Toast.LENGTH_SHORT).show()
            })
    }

}