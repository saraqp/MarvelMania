package quesadoprado.saramaria.marvelmania.data.series

data class Data(
    val offset:Int?=null,
    val limit:Int?=null,
    val total:Int?=null,
    val count:Int?=null,
    val results:Array<Serie>?=null,
)
