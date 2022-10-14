package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.data.comics.ComicsDTO
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker


class ComicsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var texto:TextView=view.findViewById(R.id.textoComics)
        RetrofitBroker.getRequestAllComics(
            onResponse = {
                var gson=Gson().newBuilder().setDateFormat("yyyy-MM-dd").create()
                var response:ComicsDTO=gson.fromJson(it,ComicsDTO::class.java)

                texto.text=response.toString()
            }, onFailure = {
                texto.text=getString(R.string.error)
            })
    }
}