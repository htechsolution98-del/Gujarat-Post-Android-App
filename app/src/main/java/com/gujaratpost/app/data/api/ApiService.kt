package com.gujaratpost.app.data.api

import com.gujaratpost.app.data.models.ApiResponse
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.data.models.ArticlesResponseData
import com.gujaratpost.app.data.models.Category
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
        @Query("isTrending") isTrending: Boolean? = null,
        @Query("isBreaking") isBreaking: Boolean? = null,
        @Query("sort") sort: String? = "latest"
    ): Response<ApiResponse<ArticlesResponseData>>

    /**
     * Get single article details by ID or Slug
     */
    @GET("api/public/articles/{slugOrId}")
    suspend fun getArticleDetail(
        @Path("slugOrId") slugOrId: String
    ): Response<ApiResponse<Article>>

    /**
     * Get active news categories
     */
    @GET("api/public/categories")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    /**
     * Get breaking news list
     */
    @GET("api/public/breaking")
    suspend fun getBreakingNews(): Response<ApiResponse<List<Article>>>

    /**
     * Get trending news list
     */
    @GET("api/public/trending")
    suspend fun getTrendingNews(): Response<ApiResponse<List<Article>>>
}
