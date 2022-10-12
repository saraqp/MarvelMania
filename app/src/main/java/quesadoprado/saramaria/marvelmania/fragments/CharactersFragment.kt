package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker

class CharactersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_characters, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var texto:TextView=view.findViewById(R.id.texto)

        RetrofitBroker.getRequestAllCharacters(
            onResponse = {
                val respuesta=Gson().fromJson(it, CharactersDTO::class.java)
                texto.text=respuesta.toString()
            }, onFailure = {
                texto.text=getString(R.string.error)

            })
    }

}