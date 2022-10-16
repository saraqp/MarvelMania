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
import com.google.gson.GsonBuilder
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.adapter.ComicAdapter
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

        val recyclerView:RecyclerView=view.findViewById(R.id.recyclerViewComics)
        recyclerView.layoutManager=GridLayoutManager(context,2)

        RetrofitBroker.getRequestAllComics(
            onResponse = {
                val gson=Gson().newBuilder().setDateFormat("yyyy-MM-dd").create()
                val respuesta:ComicsDTO=gson.fromJson(it,ComicsDTO::class.java)

                val comics=respuesta.data?.results
                val adapter=ComicAdapter(comics)
                recyclerView.adapter=adapter
                adapter.setOnItemClickListener(object :ComicAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        Toast.makeText(context,comics?.get(position).toString(),Toast.LENGTH_SHORT).show()
                    }

                })

            }, onFailure = {
                Toast.makeText(context,getString(R.string.error), Toast.LENGTH_SHORT).show()

            })
    }
}