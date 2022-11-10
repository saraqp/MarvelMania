package quesadoprado.saramaria.marvelmania.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils

class ComicFavouritesAdapter(private val list_comics: Array<Comic>?): RecyclerView.Adapter<ComicFavouritesAdapter.ViewHolder>() {
    private var context: Context? =null
    private lateinit var mListener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener=listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_list_fav,parent,false)
        context=parent.context
        return ViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comicHolder: Comic =list_comics?.get(position)!!
        val imageUrl="${comicHolder.thumbnail?.path}/portrait_uncanny.${comicHolder.thumbnail?.extension}"
        Glide.with(context!!).load(imageUrl).apply(RequestOptions().override(300,450)).into(holder.image)
        holder.nombre.text=comicHolder.title
    }
    override fun getItemCount(): Int {
        return list_comics?.size!!
    }
    class ViewHolder(itemView: View, listener: OnItemClickListener):
        RecyclerView.ViewHolder(itemView) {
        val image: ImageView =itemView.findViewById(R.id.IV_imagen)
        val nombre: TextView =itemView.findViewById(R.id.nombre)
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}