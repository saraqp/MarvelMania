package quesadoprado.saramaria.marvelmania.data.comics

import com.fasterxml.jackson.annotation.JsonFormat
import com.google.type.Date
import quesadoprado.saramaria.marvelmania.data.items.*

data class Comic(
    val id:Int?=null,
    val digitalId:Int?=null,
    val title:String?=null,
    val issueNumber:Double?=null,
    val variantDescription:String?=null,
    val description:String?=null,
    val modified: String?=null,
    val isbn:String?=null,
    val upc:String?=null,
    val diamondCode:String?=null,
    val ean:String?=null,
    val issn:String?=null,
    val format:String?=null,
    val pageCount:Int?=null,
    val textObjects:Array<TextObject>?=null,
    val resourceURI:String?=null,
    val urls:Array<Url>?=null,
    val series:Series?=null,
    val variants:Array<ItemC>?=null,
    val collections:Array<ItemC>?=null,
    val collectedIssues:Array<ItemC>?=null,
    val dates:Array<ItemCDate>?=null,
    val prices:Array<ItemCPrice>?=null,
    val images:Array<Thumbnail>?=null,
    val creator: Creator?=null,
    val characters:Characters?=null,
    val stories:Story?=null,
    val events:Events?=null
    )
