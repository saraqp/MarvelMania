package quesadoprado.saramaria.marvelmania.data.comics

import quesadoprado.saramaria.marvelmania.data.items.*

data class Comic(
    val id:Int?=null,
    val title:String?=null,
    val variantDescription:String?=null,
    val description:String?=null,
    val format:String?=null,
    val pageCount:Int?=null,
    val series: Series?=null,
    val variants:Array<Item>?=null,
    val collections:Array<Item>?=null,
    val collectedIssues:Array<Item>?=null,
    val thumbnail:Thumbnail?=null,
    val dates:Array<ItemCDate>?=null,
    val prices:Array<ItemCPrice>?=null,
    val images:Array<Thumbnail>?=null,
    val creator: Creator?=null,
    val characters: Characters?=null,
    val stories: Story?=null,
    val events: Events?=null
    )
