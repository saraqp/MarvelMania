package quesadoprado.saramaria.marvelmania.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.data.characters.Character

class ListCharactersAdapter(private val list_characters: Array<Character>?): RecyclerView.Adapter<ListCharactersAdapter.ViewHolder>() {
    private var context: Context? =null
    private lateinit var mListener:onIntemClickListener


    interface onIntemClickListener {
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener:onIntemClickListener){
        mListener=listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_list_show,parent,false)
        context=parent.context
        return ViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val characterHolder: Character = list_characters?.get(position)!!
        val imageUrl="${characterHolder.thumbnail?.path}/portrait_uncanny.${characterHolder.thumbnail?.extension}"
        Glide.with(context!!).load(imageUrl).apply(RequestOptions().override(300,450)).into(holder.image)
    }

    override fun getItemCount()=list_characters?.size!!

    class ViewHolder(itemView: View,listener: onIntemClickListener): RecyclerView.ViewHolder(itemView) {
        val image: ImageView =itemView.findViewById(R.id.IV_imagen)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}