package quesadoprado.saramaria.marvelmania.data.items

import quesadoprado.saramaria.marvelmania.data.items.ItemSt

data class Story(
    val available:Int?=null,
    val returned:Int?=null,
    val collectionUri:String?=null,
    val items:Array<ItemSt>?=null
)
