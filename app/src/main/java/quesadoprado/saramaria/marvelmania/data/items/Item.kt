package quesadoprado.saramaria.marvelmania.data.items

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val name:String?=null,
    val resourceURI:String?=null
): Parcelable