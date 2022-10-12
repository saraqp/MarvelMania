package quesadoprado.saramaria.marvelmania.data.comics

data class Data(
    val offset:Int?=null,
    val limit:Int?=null,
    val total:Int?=null,
    val count:Int?=null,
    val results:Array<Comic>?=null
)
