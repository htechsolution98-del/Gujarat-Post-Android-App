package com.gujaratpost.app.data.api

import com.gujaratpost.app.data.models.ApiResponse
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.data.models.ArticlesResponseData
import com.gujaratpost.app.data.models.CategoriesResponseData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    /**
     * Get paginated articles list with optional filters
     */
    @GET("api/public/articles")
    suspend fun getArticles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("categorySlug") categorySlug: String? = null,
        @Query("query") query: String? = null,
        @Query("isTrending") isTrending: Boolean? = null,
        @Query("isBreaking") isBreaking: Boolean? = null,
        @Query("sort") sort: String? = "latest"
    ): Response<ApiResponse<ArticlesResponseData>>

    /**
     * Get breaking news articles using query param
     */
    @GET("api/public/articles")
    suspend fun getBreakingArticles(
        @Query("isBreaking") isBreaking: Boolean = true,
        @Query("limit") limit: Int = 5
    ): Response<ApiResponse<ArticlesResponseData>>

    /**
     * Get single article details by ID or Slug
     */
    @GET("api/public/articles/{slugOrId}")
    suspend fun getArticleDetail(
        @Path("slugOrId") slugOrId: String
    ): Response<ApiResponse<com.gujaratpost.app.data.models.ArticleDetailResponseData>>

    /**
     * Get active news categories
     */
    @GET("api/public/categories")
    suspend fun getCategories(): Response<ApiResponse<CategoriesResponseData>>

    /**
     * Get video stream
     */
    @GET("api/public/videos")
    suspend fun getVideos(
        @Query("limit") limit: Int = 12
    ): Response<ApiResponse<com.gujaratpost.app.data.models.VideosResponseData>>

    /**
     * Get Instagram reels
     */
    @GET("api/public/reels")
    suspend fun getReels(): Response<ApiResponse<List<com.gujaratpost.app.data.models.Reel>>>
}

