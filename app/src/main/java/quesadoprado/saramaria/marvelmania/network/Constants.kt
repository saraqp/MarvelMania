package quesadoprado.saramaria.marvelmania.network

import java.math.BigInteger
import java.security.MessageDigest
import java.sql.Timestamp

class Constants {

    companion object{
        const val BASE_URL="https://gateway.marvel.com:443/v1/public/"
        val timeStamp = Timestamp(System.currentTimeMillis()).time.toString()
        const val API_KEY="96c2b7ce42a1a02fa020da8508dd8de3"
        const val PRIVATE_KEY="f7f8d22314eeae7a263f90ced21c5835754f2417"
        fun hash():String{
            val input="$timeStamp$PRIVATE_KEY$API_KEY"
            val md5=MessageDigest.getInstance("MD5")
            return BigInteger(1,md5.digest(input.toByteArray())).toString(16).padStart(32,'0')
        }
    }

}
