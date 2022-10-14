package quesadoprado.saramaria.marvelmania.data.series

import quesadoprado.saramaria.marvelmania.data.items.*

data class Serie (
    val id:Int?=null,
    val title:String?=null,
    val description:String?=null,
    val resourceURI:String?=null,
    val urls:Array<Url>?=null,
    val startYear:Int?=null,
    val endYear:Int?=null,
    val rating:String?=null,
    val modified:String?=null,
    val thumbnail: Thumbnail?=null,
    val comics: Comics?=null,
    val stories: Story?=null,
    val events: Events?=null,
    val characters: Characters?=null,
    val creators: Creator?=null,
    val next: Item?=null,
    val previous: Item?=null
)