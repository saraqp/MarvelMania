package quesadoprado.saramaria.marvelmania.data.comics

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import quesadoprado.saramaria.marvelmania.data.items.Characters
import quesadoprado.saramaria.marvelmania.data.items.Series
import quesadoprado.saramaria.marvelmania.data.items.Thumbnail
@Parcelize
data class Comic(
    val id:Int?=null,
    val title:String?=null,
    val variantDescription:String?=null,
    val description:String?=null,
    val format:String?=null,
    val pageCount:Int?=null,
    val thumbnail:Thumbnail?=null,
    val images:Array<Thumbnail>?=null,
    val characters: Characters?=null,
    ): Parcelable
