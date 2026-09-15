package com.gujaratpost.app.data.api

import android.content.Context
import com.gujaratpost.app.GujaratPostApp
import com.gujaratpost.app.utils.Constants
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile
    private var currentBaseUrl: String = Constants.DEFAULT_BASE_URL

    val activeApiBaseUrl: String
        get() = currentBaseUrl

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    /**
     * Smart failover interceptor: If the currently selected server is rate-limited (429),
     * offline, or unreachable, it seamlessly attempts the other known server candidates.
     */
    private val failoverInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // Build list of candidates to try, starting with active server
        val candidates = mutableListOf<String>()
        candidates.add(currentBaseUrl)
        for (c in Constants.CANDIDATE_BASE_URLS) {
            val formatted = if (c.endsWith("/")) c else "$c/"
            if (!candidates.contains(formatted)) {
                candidates.add(formatted)
            }
        }

        var lastResponse: Response? = null
        var lastException: Exception? = null

        for (candidate in candidates) {
            val candidateHttpUrl = candidate.toHttpUrlOrNull() ?: continue
            val newUrl = originalUrl.newBuilder()
                .scheme(candidateHttpUrl.scheme)
                .host(candidateHttpUrl.host)
                .port(candidateHttpUrl.port)
                .build()

            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            try {
                // Short 4-second connect timeout for fast server failover
                val customChain = chain
                    .withConnectTimeout(4, TimeUnit.SECONDS)
                    .withReadTimeout(12, TimeUnit.SECONDS)

                val response = customChain.proceed(newRequest)

                // If response is successful (200..299) or standard client status (not rate-limited 429 or 5xx server crash)
                if (response.isSuccessful || (response.code != 429 && response.code < 500)) {
                    if (currentBaseUrl != candidate) {
                        currentBaseUrl = candidate
                        saveWorkingServer(candidate)
                    }
                    return@Interceptor response
                }

                // If 429 (Render hibernate) or 5xx, try next candidate
                response.close()
                lastResponse = response
            } catch (e: Exception) {
                lastException = e
            }
        }

        if (lastResponse != null) return@Interceptor lastResponse
        throw lastException ?: IOException("All server endpoints failed")
    }

    private val headersInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestWithHeaders = original.newBuilder()
            .header("Bypass-Tunnel-Reminder", "true")
            .header("bypass-tunnel-reminder", "true")
            .header("User-Agent", "GujaratPost-Android/1.0.2")
            .build()
        chain.proceed(requestWithHeaders)
    }

    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(headersInterceptor)
            .addInterceptor(failoverInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(6, TimeUnit.SECONDS)
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
     * Initializes the client with saved user preference if available
     */
    fun initFromPreferences(context: Context) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(Constants.KEY_CUSTOM_SERVER_URL, null)
            ?: prefs.getString(Constants.KEY_ACTIVE_SERVER_URL, null)

        if (!saved.isNullOrBlank()) {
            val formatted = if (saved.endsWith("/")) saved else "$saved/"
            currentBaseUrl = formatted
            retrofitInstance = null
        }
    }

    /**
     * Explicitly set server URL (e.g. from Server Settings Dialog)
     */
    fun updateBaseUrl(newBaseUrl: String, context: Context? = null) {
        var formatted = newBaseUrl.trim()
        if (!formatted.endsWith("/")) {
            formatted += "/"
        }
        currentBaseUrl = formatted
        retrofitInstance = null

        context?.let {
            val prefs = it.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(Constants.KEY_CUSTOM_SERVER_URL, formatted)
                .putString(Constants.KEY_ACTIVE_SERVER_URL, formatted)
                .apply()
        }
    }

    private fun saveWorkingServer(url: String) {
        try {
            val prefs = GujaratPostApp.instance.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(Constants.KEY_ACTIVE_SERVER_URL, url).apply()
        } catch (e: Exception) {
            // Ignore
        }
    }
}

