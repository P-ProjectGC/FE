package com.example.plango.data

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class PerfApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val start = System.currentTimeMillis()
        val response = chain.proceed(request)
        val end = System.currentTimeMillis()

        val duration = end - start
        val path = request.url.encodedPath

        Log.d(
            "PERF_API",
            "API ${request.method} $path DURATION=${duration}ms"
        )

        return response
    }
}
