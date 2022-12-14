package quesadoprado.saramaria.marvelmania.data.characters

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import quesadoprado.saramaria.marvelmania.data.items.Thumbnail

@Parcelize
data class Character(
    val id:Int?=null,
    val name:String?=null,
    val description:String?=null,
    val thumbnail: Thumbnail?=null
    ): Parcelable
