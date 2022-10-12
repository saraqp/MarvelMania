package quesadoprado.saramaria.marvelmania.data.items

import quesadoprado.saramaria.marvelmania.data.items.ItemE

data class Events(
    val available:Int?=null,
    val returned:Int?=null,
    val collectionUri:String?=null,
    val items:Array<ItemE>?=null
)
