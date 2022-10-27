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
import quesadoprado.saramaria.marvelmania.activities.showInfo.InfoCompleteComics
import quesadoprado.saramaria.marvelmania.adapter.ComicAdapter
import quesadoprado.saramaria.marvelmania.data.comics.ComicsDTO
import quesadoprado.saramaria.marvelmania.databinding.FragmentComicsBinding
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker


class ComicsFragment(auth: FirebaseAuth) : Fragment() {

    private var _binding: FragmentComicsBinding?=null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding=FragmentComicsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewComics.layoutManager=GridLayoutManager(context,2)

        RetrofitBroker.getRequestAllComics(
            onResponse = {
                val gson=Gson().newBuilder().setDateFormat("yyyy-MM-dd").create()
                val respuesta:ComicsDTO=gson.fromJson(it,ComicsDTO::class.java)

                val comics=respuesta.data?.results
                val adapter=ComicAdapter(comics)
                binding.recyclerViewComics.adapter=adapter
                adapter.setOnItemClickListener(object :ComicAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        val comic=comics?.get(position)
                        val intent= Intent(context,InfoCompleteComics::class.java)
                        intent.putExtra("comic",comic)
                        startActivity(intent)
                    }

                })

            }, onFailure = {
                Toast.makeText(context,getString(R.string.error), Toast.LENGTH_SHORT).show()

            })
    }
}