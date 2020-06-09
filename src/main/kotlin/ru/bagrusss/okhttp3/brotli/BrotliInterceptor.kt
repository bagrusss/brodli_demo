package ru.bagrusss.okhttp3.brotli

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.http.RealResponseBody
import okio.BufferedSource
import okio.GzipSource
import okio.buffer
import okio.source
import org.brotli.dec.BrotliInputStream

/**
 * Support okhttp 3
 * https://github.com/square/okhttp/tree/master/okhttp-brotli
 */

object BrotliInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        if (chain.request().header("Accept-Encoding") == null) {
            val request = chain.request().newBuilder()
                .header("Accept-Encoding", "br,gzip")
                .build()

            chain.proceed(request).uncompress()
        } else {
            chain.proceed(chain.request())
        }

    private fun Response.uncompress(): Response {
        val body = body() ?: return this
        val encoding = header("Content-Encoding") ?: return this

        val decompressedSource = when {
            encoding.equals("br", ignoreCase = true) ->
                BrotliInputStream(body.source().inputStream()).source().buffer()
            encoding.equals("gzip", ignoreCase = true) ->
                GzipSource(body.source()).buffer()
            else -> return this
        }

        return newBuilder()
            .removeHeader("Content-Encoding")
            .removeHeader("Content-Length")
            .body(decompressedSource.asResponseBody(body.contentType(), -1))
            .build()
    }

    /** Returns a new response body that transmits this source. */
    @JvmStatic
    @JvmName("create")
    fun BufferedSource.asResponseBody(
        contentType: MediaType? = null,
        contentLength: Long = -1L
    ): ResponseBody = RealResponseBody(contentType.toString(), contentLength, this)


}
