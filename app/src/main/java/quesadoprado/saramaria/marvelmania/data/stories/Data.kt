package quesadoprado.saramaria.marvelmania.data.stories

data class Data(
    val offset:Int?=null,
    val limit:Int?=null,
    val total:Int?=null,
    val count:Int?=null,
    val results:Array<Story>?=null
)
