package quesadoprado.saramaria.marvelmania.data.items

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Series(
    val available:Int?=null,
    val returned:Int?=null,
    val collectiveUri:String?=null,
    val items:Array<Item>?=null
): Parcelable