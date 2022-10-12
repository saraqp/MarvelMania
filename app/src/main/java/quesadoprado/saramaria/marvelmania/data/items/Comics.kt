package quesadoprado.saramaria.marvelmania.data.items

import quesadoprado.saramaria.marvelmania.data.items.ItemC

data class Comics(
    val available:Int?=null,
    val returned:Int?=null,
    val collectionUri:String?=null,
    val items:Array<ItemC>?=null
)
