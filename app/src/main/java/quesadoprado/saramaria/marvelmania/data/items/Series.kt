package quesadoprado.saramaria.marvelmania.data.items

import quesadoprado.saramaria.marvelmania.data.items.ItemS

data class Series(
    val available:Int?=null,
    val returned:Int?=null,
    val collectiveUri:String?=null,
    val items:Array<ItemS>?=null
)
