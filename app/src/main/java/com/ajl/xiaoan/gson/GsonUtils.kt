package com.ajl.xiaoan.gson

import com.ajl.xiaoan.AuntDay
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDate

object GsonUtils {
    private var gson: Gson

    init {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(LocalDate::class.java, LocalDateSerializer())
        gsonBuilder.registerTypeAdapter(LocalDate::class.java, LocalDateDeserializer())
        gson = gsonBuilder.setPrettyPrinting().create()
    }

    fun <T> toList(str:String,type: Type): MutableList<T> {
        return  gson.fromJson(str,type)
    }
}