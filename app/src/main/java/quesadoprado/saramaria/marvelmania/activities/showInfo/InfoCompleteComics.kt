package quesadoprado.saramaria.marvelmania.activities.showInfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.adapter.ListCharactersAdapter
import quesadoprado.saramaria.marvelmania.adapter.ListVariantImagesComicsAdapter
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.databinding.ActivityInfocompletecomicsBinding
import quesadoprado.saramaria.marvelmania.network.RetrofitBroker
import quesadoprado.saramaria.marvelmania.data.characters.*
import java.util.Objects

class InfoCompleteComics:AppCompatActivity() {
    private lateinit var binding: ActivityInfocompletecomicsBinding
    private lateinit var context : Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfocompletecomicsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context=this
        val comic=intent?.getParcelableExtra<Comic>("comic")

        val imageUrl="${comic?.thumbnail?.path}/portrait_uncanny.${comic?.thumbnail?.extension}"

        Glide.with(this).load(imageUrl).apply(RequestOptions().override(500,650)).into(binding.imageIV)

        binding.tituloTV.text=comic?.title

        if(comic?.description.equals("")&& comic?.variantDescription.equals("")){
            binding.descripcionText.text=getString(R.string.noHayDescripcion)
        }else if(comic?.description.equals("")){
            binding.descripcionText.text=comic?.variantDescription
        }else{
            binding.descripcionText.text=comic?.description
        }
        binding.numpagText.text= comic?.pageCount.toString()
        binding.formatText.text=comic?.format

        binding.recyclerViewListVariantImages.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        var adapterimages=ListVariantImagesComicsAdapter(comic?.images)
        binding.recyclerViewListVariantImages.adapter=adapterimages

        binding.recyclerViewListCharacters.layoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
        RetrofitBroker.getRequestCharactersForComicId(
            comic?.id!!,
            onResponse = {
                val respuesta=Gson().fromJson(it,CharactersDTO::class.java)
                val characters=respuesta?.data?.results
                val adapter=ListCharactersAdapter(characters)
                binding.recyclerViewListCharacters.adapter=adapter

                adapter.setOnItemClickListener(object : ListCharactersAdapter.onIntemClickListener{
                    override fun onItemClick(position: Int) {
                        val character=characters?.get(position)
                        val intent= Intent(context,InfoCompleteCharacts::class.java)
                        intent.putExtra("charact",character)
                        startActivity(intent)
                    }

                })
            }, onFailure = {
                Log.e("ERROR_API",it)
            }
        )
    }
}