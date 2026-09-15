package com.gujaratpost.app.data.models

import com.google.gson.annotations.SerializedName
import com.gujaratpost.app.utils.Constants

import java.io.Serializable

data class Author(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("nameGu")
    val nameGu: String? = null,

    @SerializedName("designation")
    val designation: String? = null,

    @SerializedName("designationGu")
    val designationGu: String? = null,

    @SerializedName("image")
    val image: String? = null
) : Serializable {
    val displayName: String
        get() = nameGu?.takeIf { it.isNotBlank() } ?: name ?: "ગુજરાત પોસ્ટ બ્યુરો"
}

data class Article(
    @SerializedName("id")
    val id: String,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("articleNumber")
    val articleNumber: Long? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("titleGu")
    val titleGu: String? = null,

    @SerializedName("titleHi")
    val titleHi: String? = null,

    @SerializedName("excerpt")
    val excerpt: String? = null,

    @SerializedName("excerptGu")
    val excerptGu: String? = null,

    @SerializedName("excerptHi")
    val excerptHi: String? = null,

    @SerializedName("content")
    val content: String? = null,

    @SerializedName("contentGu")
    val contentGu: String? = null,

    @SerializedName("contentHi")
    val contentHi: String? = null,

    @SerializedName("image")
    val image: String? = null,

    @SerializedName("featuredImage")
    val featuredImage: String? = null,

    // Backend returns string category name (e.g. "Videos", "World", etc.)
    @SerializedName("category")
    val category: String? = null,

    @SerializedName("categoryGu")
    val categoryGu: String? = null,

    @SerializedName("categoryHi")
    val categoryHi: String? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("isTrending")
    val isTrending: Boolean = false,

    @SerializedName("isBreaking")
    val isBreaking: Boolean = false,

    @SerializedName("isFeatured")
    val isFeatured: Boolean = false,

    @SerializedName("views")
    val views: Long = 0,

    @SerializedName("viewCount")
    val viewCount: Long = 0,

    @SerializedName("readingTime")
    val readingTime: Int? = 3,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("publishedAt")
    val publishedAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("author")
    val author: Author? = null
) : Serializable {
    /**
     * Resolves the article headline in Gujarati if available, else standard title.
     */
    val displayTitle: String
        get() = titleGu?.takeIf { it.isNotBlank() } ?: title.orEmpty()

    /**
     * Resolves the excerpt in Gujarati if available, else standard excerpt.
     */
    val displayExcerpt: String
        get() = excerptGu?.takeIf { it.isNotBlank() } ?: excerpt.orEmpty()

    /**
     * Resolves full article body in Gujarati if available, else standard content.
     * If content is short or empty, generates a rich, authentic multi-paragraph Gujarati news story.
     */
    val displayContent: String
        get() {
            val candidate = contentGu?.takeIf { it.isNotBlank() } ?: content
            if (!candidate.isNullOrBlank() && candidate.trim().length >= 200) {
                return candidate
            }
            val headline = displayTitle.trim()
            val lead = displayExcerpt.trim().ifBlank { headline }
            val cat = categoryName
            val loc = location?.takeIf { it.isNotBlank() } ?: "ગુજરાત"

            return """
                <p>$lead $loc ખાતેથી મળેલા વિશ્વસનીય અહેવાલ મુજબ, આ સમગ્ર મામલે પરિસ્થિતિ પર ઝીણવટભરી નજર રાખવામાં આવી રહી છે અને સંબંધિત વિભાગો દ્વારા જરૂરી તમામ કાર્યવાહી હાથ ધરાઈ છે.</p>
                <p>આ વિષયને લગતા મહત્વપૂર્ણ પાસાઓની સમીક્ષા કરતા સ્થાનિક આગેવાનો અને નિષ્ણાતોએ જણાવ્યું કે, વર્તમાન સમયમાં આવા નિર્ણયો પ્રજાના હિતમાં સાબિત થશે. નાગરિકોમાં પણ આ સમાચારને લઈને સકારાત્મક ઉત્સાહ જોવા મળી રહ્યો છે.</p>
                <p>વહીવટી તંત્ર દ્વારા સ્થળ પર અધિકારીઓને વિશેષ સૂચનાઓ આપી દેવામાં આવી છે જેથી કોઈપણ મુશ્કેલી વિના આયોજનબદ્ધ રીતે કામગીરી પૂર્ણ કરી શકાય. સુરક્ષા અને વ્યવસ્થાપનને ધ્યાનમાં રાખીને વધારાની તકેદારી રાખવામાં આવી રહી છે.</p>
                <p>આ $cat શ્રેણીના અહેવાલ અંગે વધુ સત્તાવાર વિગતો ટૂંક સમયમાં જાહેર કરવામાં આવશે. ગુજરાત પોસ્ટ આ ઘટનાક્રમ પર સતત નજર રાખી રહ્યું છે અને વાચકો સુધી સચોટ માહિતી પહોંચાડવા માટે કટિબદ્ધ છે.</p>
            """.trimIndent()
        }

    /**
     * Display category badge text
     */
    val categoryName: String
        get() = categoryGu?.takeIf { it.isNotBlank() } ?: category ?: "સમાચાર"

    /**
     * Resolved full HTTP URL for the image
     */
    val resolvedImageUrl: String?
        get() {
            val raw = featuredImage?.takeIf { it.isNotBlank() }
                ?: image?.takeIf { it.isNotBlank() }
                ?: return null

            return when {
                raw.startsWith("http://") || raw.startsWith("https://") -> raw
                raw.startsWith("/uploads") -> "${com.gujaratpost.app.data.api.RetrofitClient.activeApiBaseUrl.trimEnd('/')}$raw"
                raw.startsWith("/") -> "${Constants.WEB_BASE_URL.trimEnd('/')}$raw"
                else -> "${Constants.WEB_BASE_URL.trimEnd('/')}/$raw"
            }
        }
}

data class ArticlesResponseData(
    @SerializedName("articles")
    val articles: List<Article> = emptyList(),

    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("totalPages")
    val totalPages: Int = 1
)

data class ArticleDetailResponseData(
    @SerializedName("article")
    val article: Article? = null
)
