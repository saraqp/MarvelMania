package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.adapter.CharacterAdapter
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
        val recyclerView:RecyclerView=view.findViewById(R.id.recyclerViewCharacters)
        recyclerView.layoutManager=GridLayoutManager(context,2)

        RetrofitBroker.getRequestAllCharacters(
            onResponse = {
                val respuesta=Gson().fromJson(it, CharactersDTO::class.java)
                val characters=respuesta?.data?.results

                val adapter=CharacterAdapter(characters)
                recyclerView.adapter=adapter

                adapter.setOnItemClickListener(object : CharacterAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {

                        Toast.makeText(context,characters?.get(position).toString(),Toast.LENGTH_SHORT).show()
                    }

                })

            }, onFailure = {
                Toast.makeText(context,getString(R.string.error),Toast.LENGTH_SHORT).show()

            })
    }

}