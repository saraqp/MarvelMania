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
import quesadoprado.saramaria.marvelmania.activities.showInfo.InfoCompleteCharacts
import quesadoprado.saramaria.marvelmania.adapter.CharacterAdapter
import quesadoprado.saramaria.marvelmania.data.characters.CharactersDTO
import quesadoprado.saramaria.marvelmania.databinding.FragmentCharactersBinding
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker

class CharactersFragment(auth: FirebaseAuth) : Fragment() {

    private var _binding:FragmentCharactersBinding?=null
    private val binding get() = _binding!!

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
        RetrofitBroker.getRequestAllCharacters(
            onResponse = {
                val respuesta=Gson().fromJson(it, CharactersDTO::class.java)
                val characters=respuesta?.data?.results

                val adapter=CharacterAdapter(characters)
                binding.recyclerViewCharacters.adapter=adapter

                adapter.setOnItemClickListener(object : CharacterAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        val character=characters?.get(position)
                        val intent=Intent(context,InfoCompleteCharacts::class.java)
                        intent.putExtra("charact",character)
                        startActivity(intent)
                    }

                })

            }, onFailure = {
                Toast.makeText(context,getString(R.string.error),Toast.LENGTH_SHORT).show()

            })
    }

}