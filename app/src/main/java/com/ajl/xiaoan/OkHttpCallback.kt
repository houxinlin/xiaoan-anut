package com.ajl.xiaoan

import okhttp3.Callback

fun interface  OkHttpCallback  {
    fun success(body:String)
}
