package quesadoprado.saramaria.marvelmania.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.data.characters.Character

class CharacterAdapter(private val list_characters: Array<Character>?): RecyclerView.Adapter<CharacterAdapter.ViewHolder>() {

    private var context: Context? =null
    private lateinit var mListener:onIntemClickListener
    private lateinit var mlongListener:onIntemLongClickListener

    interface onIntemClickListener {
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener:onIntemClickListener){
        mListener=listener
    }
    interface onIntemLongClickListener {
        fun onItemLongClick(position: Int,view:View):Boolean
    }
    fun setOnItemLongClickListener(listener:onIntemLongClickListener){
        mlongListener=listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_list,parent,false)
        context=parent.context
        return ViewHolder(view,mListener,mlongListener)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val characterHolder: Character = list_characters?.get(position)!!
        val imageUrl="${characterHolder.thumbnail?.path}/portrait_uncanny.${characterHolder.thumbnail?.extension}"
        Glide.with(context!!).load(imageUrl).apply(RequestOptions().override(300,450)).into(holder.image)
        holder.nombre.text=characterHolder.name
    }

    override fun getItemCount(): Int {
        return list_characters?.size!!
    }
    class ViewHolder(
        itemView: View,
        listener: onIntemClickListener,
        longlistener: onIntemLongClickListener
    ):RecyclerView.ViewHolder(itemView) {
        val image:ImageView=itemView.findViewById(R.id.IV_imagen)
        val nombre:TextView=itemView.findViewById(R.id.nombre)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            itemView.setOnLongClickListener {
                longlistener.onItemLongClick(adapterPosition,itemView)
            }
        }
    }
}

