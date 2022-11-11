package quesadoprado.saramaria.marvelmania.data.items

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Characters(
    val items:Array<ItemCSum>?=null
): Parcelable