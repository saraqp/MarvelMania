package quesadoprado.saramaria.marvelmania.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.data.util.Coment
import quesadoprado.saramaria.marvelmania.interfaces.OnComentClickListener
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class ComentAdapter(private val list_coments: Array<Coment>?): RecyclerView.Adapter<ComentAdapter.ViewHolder>() {

    private var context: Context? =null
    private lateinit var mListener: OnComentClickListener
    private val database=firebaseDatabase
    private val auth= firebaseAuth

    private var holder:ViewHolder?=null
    fun setOnItemClickListener(listener: OnComentClickListener){
        mListener=listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_list_coments,parent,false)
        context=parent.context
        return ViewHolder(view,mListener)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comentHolder: Coment = list_coments?.get(position)!!
        this.holder=holder
        holder.coment.text=comentHolder.comentario
        holder.score.text= comentHolder.puntuacion.toString()
        holder.username.text=comentHolder.username
        if (comentHolder.id_coment_resp!!.toString()=="null"){
            holder.respComent.visibility=View.GONE
        }else{
            holder.respComent.visibility=View.VISIBLE
            obtenerTextoComentarioResp(comentHolder.id_coment_resp!!,holder)
        }
        comprobarVotoUsuario(comentHolder.idComent,holder)

    }

    private fun comprobarVotoUsuario(idComent: String?, holder: ViewHolder) {
        database.collection("users/${auth.currentUser!!.uid}/comentsVotes")
            .document(idComent.toString()).get().addOnCompleteListener {comentario->
                if (comentario.isSuccessful){
                    if (comentario.result.exists()){
                        when(comentario.result.data!!["vote"].toString()){
                            "upvote"->{
                                holder.upvote.tag=context!!.getString(R.string.votado)
                                holder.upvote.setImageResource(R.drawable.ic_upvotes_voted)
                            }
                            "downvote"->{
                                holder.downvote.tag=context!!.getString(R.string.votado)
                                holder.downvote.setImageResource(R.drawable.ic_downvotes_voted)
                            }
                        }
                    }
                }
            }
    }

    override fun getItemCount(): Int {
        return list_coments?.size!!
    }

    private fun obtenerTextoComentarioResp(id_resp: String, holder: ViewHolder){
        database.collection("coments").document(id_resp).get()
            .addOnCompleteListener { document->
            if (document.isSuccessful){
                if (document.result.exists()){
                    val coment=document.result.data
                    holder.respComent.text= coment!!["coment"].toString()
                }
            }
        }
    }

    class ViewHolder(itemView: View,listener: OnComentClickListener): RecyclerView.ViewHolder(itemView) {
        val respComent:TextView=itemView.findViewById(R.id.respComent)
        val coment:TextView=itemView.findViewById(R.id.TVComent)
        val upvote:ImageView =itemView.findViewById(R.id.upVote)
        val downvote:ImageView=itemView.findViewById(R.id.downVote)
        val reply:ImageView=itemView.findViewById(R.id.reply)
        val score:TextView=itemView.findViewById(R.id.TVpuntuacionText)
        val username:TextView=itemView.findViewById(R.id.user_coment)
        init {
            reply.setOnClickListener {
                listener.onReplyClick(adapterPosition)
            }
            upvote.setOnClickListener {
                listener.onUpVoteClick(adapterPosition,upvote,downvote)
            }
            downvote.setOnClickListener {
                listener.onDownVoteClick(adapterPosition,downvote,upvote)
            }
        }

    }
}
