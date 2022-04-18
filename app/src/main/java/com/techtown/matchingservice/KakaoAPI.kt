package com.techtown.matchingservice

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoAPI {
    @GET("v2/local/search/category.json")
    fun getSearchKeyword(
        @Header("Authorization") key : String,
        @Query("category_group_code") category_group_code : String,
        @Query("x") x : String,
        @Query("y") y : String,
        @Query("radius") radius : Int,
        @Query("size") size : Int,
        @Query("sort") distance : String
    ) : Call<ResultSearchKeyword>

    @GET("v2/local/search/category.json")
    fun getSearchCategory(
        @Header("Authorization") Key : String,
        @Query("category.json") category_group_code : String,
        @Query("x") x : String,
        @Query("y") y : String,
        @Query("radius") radius : Int
    ): Call<ResultSearchKeyword>
}