package quesadoprado.saramaria.marvelmania.data.util

data class Coment(
    var type:String,
    var id_type:Int,
    var username:String,
    var id_userComent:String,
    var puntuacion:Int?=null,
    var comentario:String?=null,
    var id_coment_resp:String?=null,
    var idComent:String?=null
)
