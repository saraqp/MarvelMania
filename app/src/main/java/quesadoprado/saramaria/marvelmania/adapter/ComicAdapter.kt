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
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.interfaces.OnItemLongClickListener
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase


class ComicAdapter(private val list_comics: Array<Comic>?) :
    RecyclerView.Adapter<ComicAdapter.ViewHolder>() {

    private var context: Context? = null
    private lateinit var mListener: OnItemClickListener
    private lateinit var mLongListener: OnItemLongClickListener
    private val database = firebaseDatabase
    private val currentUser = firebaseAuth.currentUser

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        mLongListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        context = parent.context
        return ViewHolder(view, mListener, mLongListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comicHolder: Comic = list_comics?.get(position)!!

        val imageUrl =
            "${comicHolder.thumbnail?.path}/portrait_uncanny.${comicHolder.thumbnail?.extension}"
        Glide.with(context!!).load(imageUrl).apply(RequestOptions().override(300, 450))
            .into(holder.image)
        holder.nombre.text = comicHolder.title
        if (firebaseAuth.currentUser != null) {
            comprobarFav(holder, comicHolder)
        } else {
            holder.icFav.visibility = View.GONE
        }
    }

    private fun comprobarFav(holder: ViewHolder, comicHolder: Comic) {
        database.collection("users/${currentUser!!.uid}/comics").document(comicHolder.id.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    holder.icFav.visibility = View.VISIBLE
                } else {
                    holder.icFav.visibility = View.GONE
                }
            }
    }

    override fun getItemCount(): Int {
        return list_comics?.size!!
    }

    class ViewHolder(
        itemView: View,
        listener: OnItemClickListener,
        longListener: OnItemLongClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.IV_imagen)
        val nombre: TextView = itemView.findViewById(R.id.nombre)
        val icFav: ImageView = itemView.findViewById(R.id.iconFav)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            itemView.setOnLongClickListener {
                longListener.onItemLongClick(adapterPosition, itemView)
            }
        }
    }
}