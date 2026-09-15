package com.gujaratpost.app.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.data.models.Author

object NewsCacheManager {

    private const val PREFS_NAME = "gujarat_post_news_cache"
    private const val KEY_CACHED_ARTICLES = "cached_home_articles_json"
    private val gson = Gson()

    fun getCachedArticles(context: Context): List<Article> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CACHED_ARTICLES, null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<Article>>() {}.type
                val parsed: List<Article>? = gson.fromJson(json, type)
                if (!parsed.isNullOrEmpty()) {
                    return parsed
                }
            } catch (e: Exception) {
                // Fallback to defaults
            }
        }
        return getDefaultArticles()
    }

    fun saveCachedArticles(context: Context, articles: List<Article>) {
        if (articles.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CACHED_ARTICLES, gson.toJson(articles)).apply()
    }

    private fun getDefaultArticles(): List<Article> {
        val defaultAuthor = Author(
            id = "team-1",
            name = "Gujarat Post Team",
            nameGu = "ગુજરાત પોસ્ટ બ્યુરો",
            designation = "Editorial Bureau",
            designationGu = "સમાચાર ડેસ્ક"
        )

        return listOf(
            Article(
                id = "art-metro-phase2",
                slug = "videos-1787394463562-1-791",
                title = "વીડિયો રિપોર્ટ: અમદાવાદ મેટ્રો ટ્રેનના નવા ફેઝ-2 ના ટ્રાયલ રનનો અંદરનો અદ્ભુત નજારો જુઓ",
                titleGu = "વીડિયો રિપોર્ટ: અમદાવાદ મેટ્રો ટ્રેનના નવા ફેઝ-2 ના ટ્રાયલ રનનો અંદરનો અદ્ભુત નજારો જુઓ",
                excerpt = "અમારા સંવાદદાતાએ મેટ્રો કોચમાંથી લાઈવ કવરેજ કરી મુસાફરોની સુવિધાઓ બતાવી.",
                excerptGu = "અમારા સંવાદદાતાએ મેટ્રો કોચમાંથી લાઈવ કવરેજ કરી મુસાફરોની સુવિધાઓ બતાવી.",
                content = "અમદાવાદમાં મેટ્રો ટ્રેનની સ્પીડ અને અદ્યતન સુવિધાઓ દર્શાવતો ખાસ વિડીયો રિપોર્ટ. સ્ટેશન પરના સિક્યુરિટી અને ટિકિટિંગ સિસ્ટમનો ચિતાર.",
                contentGu = "અમદાવાદમાં મેટ્રો ટ્રેનની સ્પીડ અને અદ્યતન સુવિધાઓ દર્શાવતો ખાસ વિડીયો રિપોર્ટ. સ્ટેશન પરના સિક્યુરિટી અને ટિકિટિંગ સિસ્ટમનો ચિતાર.",
                image = "https://gujaratpost.vercel.app/assets/demo/5.jpg",
                featuredImage = "https://gujaratpost.vercel.app/assets/demo/5.jpg",
                category = "Videos",
                categoryGu = "વીડિયો",
                location = "Ahmedabad",
                author = defaultAuthor,
                publishedAt = "2026-08-21T19:27:43.562Z",
                views = 841,
                isFeatured = true
            ),
            Article(
                id = "art-g20-summit",
                slug = "world-1787394462182-1-175",
                title = "જી-20 શિખર સંમેલનમાં વૈશ્વિક આર્થિક સ્થિરતા અને એઆઈ ગવર્નન્સ અંગે ઐતિહાસિક સમજૂતી કરાર",
                titleGu = "જી-20 શિખર સંમેલનમાં વૈશ્વિક આર્થિક સ્થિરતા અને એઆઈ ગવર્નન્સ અંગે ઐતિહાસિક સમજૂતી કરાર",
                excerpt = "વિશ્વના 20 અગ્રણી દેશોએ આર્ટિફિશિયલ ઇન્ટેલિજન્સના સુરક્ષિત ઉપયોગ માટે વૈશ્વિક ફ્રેમવર્ક સ્વીકાર્યું.",
                excerptGu = "વિશ્વના 20 અગ્રણી દેશોએ આર્ટિફિશિયલ ઇન્ટેલિજન્સના સુરક્ષિત ઉપયોગ માટે વૈશ્વિક ફ્રેમવર્ક સ્વીકાર્યું.",
                content = "જી-20 દેશોના વડાઓએ વૈશ્વિક આર્થિક પડકારો અને નવી ટેકનોલોજીના યોગ્ય નિયમન માટે સંયુક્ત ઘોષણાપત્ર જારી કર્યું.",
                contentGu = "જી-20 દેશોના વડાઓએ વૈશ્વિક આર્થિક પડકારો અને નવી ટેકનોલોજીના યોગ્ય નિયમન માટે સંયુક્ત ઘોષણાપત્ર જારી કર્યું.",
                image = "https://gujaratpost.vercel.app/assets/demo/5.jpg",
                featuredImage = "https://gujaratpost.vercel.app/assets/demo/5.jpg",
                category = "World",
                categoryGu = "વિશ્વ",
                location = "Geneva",
                author = defaultAuthor,
                publishedAt = "2026-08-21T19:57:42.182Z",
                views = 574
            ),
            Article(
                id = "art-rain-forecast-gujarat",
                slug = "gujarat-rain-forecast-update",
                title = "રાજ્યમાં આગામી દિવસોમાં સાર્વત્રિક વરસાદની આગાહી: હવામાન વિભાગનો રિપોર્ટ",
                titleGu = "રાજ્યમાં આગામી દિવસોમાં સાર્વત્રિક વરસાદની આગાહી: હવામાન વિભાગનો રિપોર્ટ",
                excerpt = "દક્ષિણ ગુજરાત અને સૌરાષ્ટ્રના દરિયાકાંઠાના વિસ્તારોમાં ભારે વરસાદની સંભાવના વ્યક્ત કરાઈ.",
                excerptGu = "દક્ષિણ ગુજરાત અને સૌરાષ્ટ્રના દરિયાકાંઠાના વિસ્તારોમાં ભારે વરસાદની સંભાવના વ્યક્ત કરાઈ.",
                content = "હવામાન વિભાગ અનુસાર બંગાળની ખાડીમાં સિસ્ટમ સક્રિય થતાં ગુજરાતમાં વરસાદી માહોલ જળવાઈ રહેશે.",
                contentGu = "હવામાન વિભાગ અનુસાર બંગાળની ખાડીમાં સિસ્ટમ સક્રિય થતાં ગુજરાતમાં વરસાદી માહોલ જળવાઈ રહેશે.",
                image = "https://gujaratpost.vercel.app/assets/demo/5.jpg",
                featuredImage = "https://gujaratpost.vercel.app/assets/demo/5.jpg",
                category = "Gujarat",
                categoryGu = "ગુજરાત",
                location = "Gandhinagar",
                author = defaultAuthor,
                publishedAt = "2026-08-22T08:00:00.000Z",
                views = 1205
            ),
            Article(
                id = "art-gift-city-fintech",
                slug = "business-gift-city-expansion",
                title = "ગિફ્ટ સિટીમાં વધુ 15 ગ્લોબલ ફિનટેક કંપનીઓ શરૂ કરશે કામગીરી: 10,000 નવી નોકરીઓનું સર્જન",
                titleGu = "ગિફ્ટ સિટીમાં વધુ 15 ગ્લોબલ ફિનટેક કંપનીઓ શરૂ કરશે કામગીરી: 10,000 નવી નોકરીઓનું સર્જન",
                excerpt = "ગાંધીનગર સ્થિત ગિફ્ટ સિટીમાં આંતરરાષ્ટ્રીય નાણાકીય સંસ્થાઓનું રોકાણ વધ્યું.",
                excerptGu = "ગાંધીનગર સ્થિત ગિફ્ટ સિટીમાં આંતરરાષ્ટ્રીય નાણાકીય સંસ્થાઓનું રોકાણ વધ્યું.",
                content = "ગુજરાત ઇન્ટરનેશનલ ફાઇનાન્સ ટેક-સિટી (GIFT City) વૈશ્વિક ફાઇનાન્સ હબ તરીકે ઝડપથી ઊભરી રહ્યું છે.",
                contentGu = "ગુજરાત ઇન્ટરનેશનલ ફાઇનાન્સ ટેક-સિટી (GIFT City) વૈશ્વિક ફાઇનાન્સ હબ તરીકે ઝડપથી ઊભરી રહ્યું છે.",
                image = "https://gujaratpost.vercel.app/assets/demo/5.jpg",
                featuredImage = "https://gujaratpost.vercel.app/assets/demo/5.jpg",
                category = "Business",
                categoryGu = "વેપાર",
                location = "GIFT City",
                author = defaultAuthor,
                publishedAt = "2026-08-23T10:30:00.000Z",
                views = 980
            ),
            Article(
                id = "art-sports-cricket-india",
                slug = "sports-india-series-win",
                title = "ટીમ ઈન્ડિયાનો શાનદાર વિજય: સીરિઝમાં મેળવી અજેય સરસાઈ, યુવા ખેલાડીઓનું દમદાર પ્રદર્શન",
                titleGu = "ટીમ ઈન્ડિયાનો શાનદાર વિજય: સીરિઝમાં મેળવી અજેય સરસાઈ, યુવા ખેલાડીઓનું દમદાર પ્રદર્શન",
                excerpt = "ઓલરાઉન્ડ પ્રદર્શનના જોરે ભારતે પ્રતિસ્પર્ધી ટીમને કારમી હાર આપી.",
                excerptGu = "ઓલરાઉન્ડ પ્રદર્શનના જોરે ભારતે પ્રતિસ્પર્ધી ટીમને કારમી હાર આપી.",
                content = "ભારતીય ક્રિકેટ ટીમે જોરદાર રમત બતાવીને શ્રેણી પોતાના નામે કરી લીધી છે.",
                contentGu = "ભારતીય ક્રિકેટ ટીમે જોરદાર રમત બતાવીને શ્રેણી પોતાના નામે કરી લીધી છે.",
                image = "https://gujaratpost.vercel.app/assets/demo/5.jpg",
                featuredImage = "https://gujaratpost.vercel.app/assets/demo/5.jpg",
                category = "Sports",
                categoryGu = "સ્પોર્ટ્સ",
                location = "Ahmedabad",
                author = defaultAuthor,
                publishedAt = "2026-08-24T14:15:00.000Z",
                views = 1530
            )
        )
    }
}
