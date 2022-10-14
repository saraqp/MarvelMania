package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
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
        var texto:TextView=view.findViewById(R.id.textSeries)
        RetrofitBroker.getRequestAllSeries(
            onResponse = {
                var gson=Gson().newBuilder().setDateFormat("yyyy-MM-dd").create()
                var response: SeriesDTO =gson.fromJson(it, SeriesDTO::class.java)

                texto.text=response.toString()
            }, onFailure = {
                texto.text=getString(R.string.error)
            })
    }

}