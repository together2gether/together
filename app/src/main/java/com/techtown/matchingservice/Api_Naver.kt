package com.techtown.matchingservice

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class Api_Naver {
    val clientId = "J1nu_UfuVjUnvnvQFjCG"
    val clientSecret = "xzgFjMymr7"

    fun main(){
        var text: String? = null
        try{
            text = URLEncoder.encode("그린팩토리", "UTF-8")
        } catch (e: UnsupportedEncodingException){

        }
    }
}