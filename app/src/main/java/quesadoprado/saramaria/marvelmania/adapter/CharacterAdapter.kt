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
import quesadoprado.saramaria.marvelmania.interfaces.OnItemClickListener
import quesadoprado.saramaria.marvelmania.interfaces.OnItemLongClickListener
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class CharacterAdapter(private val list_characters: Array<Character>?) :
    RecyclerView.Adapter<CharacterAdapter.ViewHolder>() {

    private var context: Context? = null
    private lateinit var mListener: OnItemClickListener
    private lateinit var mlongListener: OnItemLongClickListener
    private val database = firebaseDatabase
    private val currentUser = firebaseAuth.currentUser

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        mlongListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        context = parent.context
        return ViewHolder(view, mListener, mlongListener)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val characterHolder: Character = list_characters?.get(position)!!
        //url para que mostrar la imagen
        val imageUrl =
            "${characterHolder.thumbnail?.path}/portrait_uncanny.${characterHolder.thumbnail?.extension}"
        Glide.with(context!!).load(imageUrl).apply(RequestOptions().override(300, 450))
            .into(holder.image)
        holder.nombre.text = characterHolder.name
        if (currentUser != null) {
            comprobarFav(holder, characterHolder)
        } else {
            holder.icFav.visibility = View.GONE
        }
    }

    private fun comprobarFav(holder: ViewHolder, characterHolder: Character) {
        database.collection("users/${currentUser!!.uid}/characters")
            .document(characterHolder.id.toString()).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    holder.icFav.visibility = View.VISIBLE
                } else {
                    holder.icFav.visibility = View.GONE
                }
            }
    }

    override fun getItemCount(): Int {
        return list_characters?.size!!
    }

    class ViewHolder(
        itemView: View,
        listener: OnItemClickListener,
        longlistener: OnItemLongClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.IV_imagen)
        val nombre: TextView = itemView.findViewById(R.id.nombre)
        val icFav: ImageView = itemView.findViewById(R.id.iconFav)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            itemView.setOnLongClickListener {
                longlistener.onItemLongClick(adapterPosition, itemView)
            }
        }
    }
}

