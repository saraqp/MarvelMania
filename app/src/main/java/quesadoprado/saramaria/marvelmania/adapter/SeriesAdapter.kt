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
import quesadoprado.saramaria.marvelmania.data.series.Serie

class SeriesAdapter(private val list_series: Array<Serie>?): RecyclerView.Adapter<SeriesAdapter.ViewHolder>() {

    private var context: Context?=null
    private lateinit var mListener:onIntemClickListener

    interface onIntemClickListener {
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener:onIntemClickListener){
        mListener=listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_list,parent,false)
        context=parent.context
        return ViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val seriesHolder:Serie=list_series?.get(position)!!
        val imageUrl="${seriesHolder.thumbnail?.path}/portrait_uncanny.${seriesHolder.thumbnail?.extension}"
        Glide.with(context!!).load(imageUrl).apply(RequestOptions().override(300,450)).into(holder.image)
        holder.nombre.text=seriesHolder.title
    }

    override fun getItemCount(): Int {
        return list_series?.size!!
    }

    class ViewHolder(itemView: View, listener:onIntemClickListener):RecyclerView.ViewHolder(itemView) {
        val image: ImageView =itemView.findViewById(R.id.IV_imagen)
        val nombre: TextView =itemView.findViewById(R.id.nombre)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}