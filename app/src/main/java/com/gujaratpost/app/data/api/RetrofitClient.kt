package com.gujaratpost.app.data.api

import com.gujaratpost.app.GujaratPostApp
import com.gujaratpost.app.utils.Constants
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var currentBaseUrl = Constants.DEFAULT_BASE_URL

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        try {
            val cacheDir = File(GujaratPostApp.instance.cacheDir, "http_cache")
            builder.cache(Cache(cacheDir, 20L * 1024 * 1024))
        } catch (e: Exception) {
            // Ignore if context not yet initialized
        }

        builder.build()
    }

    private var retrofitInstance: Retrofit? = null

    private fun getRetrofit(): Retrofit {
        if (retrofitInstance == null) {
            retrofitInstance = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofitInstance!!
    }

    val apiService: ApiService
        get() = getRetrofit().create(ApiService::class.java)

    /**
     * Allows updating the base URL dynamically at runtime (e.g. switching between local emulator, Wi-Fi LAN IP, or production server).
     */
    fun updateBaseUrl(newBaseUrl: String) {
        var formatted = newBaseUrl.trim()
        if (!formatted.endsWith("/")) {
            formatted += "/"
        }
        currentBaseUrl = formatted
        retrofitInstance = null // Invalidate instance to rebuild with new URL
    }
}
