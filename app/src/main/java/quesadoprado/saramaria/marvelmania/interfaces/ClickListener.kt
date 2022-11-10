package quesadoprado.saramaria.marvelmania.interfaces

import android.view.View

interface OnItemClickListener {
    fun onItemClick(position:Int)
}
interface OnItemLongClickListener{
    fun onItemLongClick(position: Int,view: View):Boolean
}