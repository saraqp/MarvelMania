package quesadoprado.saramaria.marvelmania.data.characters

import quesadoprado.saramaria.marvelmania.data.items.*
import java.util.*

data class Character(
    val id:Int?=null,
    val name:String?=null,
    val thumbnail: Thumbnail?=null,
    val comics: Comics?=null,
    val events: Events?=null,
    val series: Series?=null,
    val stories: Story?=null

    )
