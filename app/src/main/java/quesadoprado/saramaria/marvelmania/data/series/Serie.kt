package quesadoprado.saramaria.marvelmania.data.series

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import quesadoprado.saramaria.marvelmania.data.items.*

@Parcelize
data class Serie (
    val id:Int?=null,
    val title:String?=null,
    val description:String?=null,
    val startYear:Int?=null,
    val endYear:Int?=null,
    val rating:String?=null,
    val thumbnail: Thumbnail?=null,
    val comics: Comics?=null,
    val characters: Characters?=null,
    val next: Item?=null,
    val previous: Item?=null
): Parcelable