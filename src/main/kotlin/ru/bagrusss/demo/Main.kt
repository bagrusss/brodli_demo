package ru.bagrusss.demo

import okhttp3.OkHttpClient
import okhttp3.Request
import ru.bagrusss.okhttp3.brotli.BrotliInterceptor

val client: OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(BrotliInterceptor)
    .build()

private const val URL = "https://www.if-not-true-then-false.com"

fun main(args: Array<String>) {
    val request = Request.Builder()
        .url(URL)
        .build()

    val response = client.newCall(request).execute()

    println("response ${response.body()?.string()}")
}