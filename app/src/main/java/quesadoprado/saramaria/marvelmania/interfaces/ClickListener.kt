package quesadoprado.saramaria.marvelmania.interfaces

import android.view.View
import android.widget.ImageView

interface OnItemClickListener {
    fun onItemClick(position: Int)
}

interface OnItemLongClickListener {
    fun onItemLongClick(position: Int, view: View): Boolean
}

interface OnComentClickListener {
    fun onReplyClick(position: Int)
    fun onUpVoteClick(position: Int, holder: ImageView, downvote: ImageView)
    fun onDownVoteClick(position: Int, holder: ImageView, upvote: ImageView)
}