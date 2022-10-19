package quesadoprado.saramaria.marvelmania.data.items

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Characters(
    val available:Int?=null,
    val returned:Int?=null,
    val collectionURI:String?=null,
    val items:Array<ItemCSum>?=null
): Parcelable