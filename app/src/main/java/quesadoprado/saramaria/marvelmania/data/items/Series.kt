package quesadoprado.saramaria.marvelmania.data.items

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Series(
    val items:Array<Item>?=null
): Parcelable