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
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.interfaces.OnItemLongClickListener
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class SeriesAdapter(private val list_series: Array<Serie>?): RecyclerView.Adapter<SeriesAdapter.ViewHolder>() {

    private var context: Context?=null
    private lateinit var mListener:OnItemClickListener
    private lateinit var mLongListener:OnItemLongClickListener
    private val database= firebaseDatabase
    private val currentUser= firebaseAuth.currentUser

    fun setOnItemClickListener(listener:OnItemClickListener){
        mListener=listener
    }
    fun setOnItemLongClickListener(listener: OnItemLongClickListener){
        mLongListener=listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_list,parent,false)
        context=parent.context
        return ViewHolder(view,mListener,mLongListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val seriesHolder:Serie=list_series?.get(position)!!
        val imageUrl="${seriesHolder.thumbnail?.path}/portrait_uncanny.${seriesHolder.thumbnail?.extension}"
        Glide.with(context!!).load(imageUrl).apply(RequestOptions().override(300,450)).into(holder.image)
        holder.nombre.text=seriesHolder.title
        if (currentUser!=null){
            comprobarFav(holder,seriesHolder)
        }else{
            holder.icFav.visibility=View.GONE
        }
    }
    private fun comprobarFav(holder: ViewHolder, serieHolder: Serie) {
        database.collection("users/${currentUser!!.uid}/series").document(serieHolder.id.toString()).get()
            .addOnCompleteListener { document->
                if (document.isSuccessful){
                    if (document.result.exists()){
                        holder.icFav.visibility=View.VISIBLE
                    }else{
                        holder.icFav.visibility=View.GONE
                    }
                }
            }
    }
    override fun getItemCount(): Int {
        return list_series?.size!!
    }

    class ViewHolder(itemView: View, listener:OnItemClickListener,longListener:OnItemLongClickListener):RecyclerView.ViewHolder(itemView) {
        val image: ImageView =itemView.findViewById(R.id.IV_imagen)
        val nombre: TextView =itemView.findViewById(R.id.nombre)
        val icFav:ImageView=itemView.findViewById(R.id.iconFav)
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            itemView.setOnLongClickListener {
                longListener.onItemLongClick(adapterPosition,itemView)
            }
        }
    }
}