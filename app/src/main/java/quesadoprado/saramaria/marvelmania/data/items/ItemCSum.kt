package quesadoprado.saramaria.marvelmania.data.items

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemCSum(
    val resourceURI:String?=null,
    val name:String?=null,
    val role:String?=null
): Parcelable