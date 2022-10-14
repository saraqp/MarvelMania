package quesadoprado.saramaria.marvelmania.data.stories

import quesadoprado.saramaria.marvelmania.data.items.*

data class Story(
    val id:Int?=null,
    val title:String?=null,
    val description:String?=null,
    val resourceUri:String?=null,
    val type:String?=null,
    val modified:String?=null,
    val thumbnail:Thumbnail?=null,
    val comics:Comics?=null,
    val series:Series?=null,
    val events:Events?=null,
    val characters: Characters?=null,
    val creators:Creator?=null,
    val originalIssue:Item?=null
)
