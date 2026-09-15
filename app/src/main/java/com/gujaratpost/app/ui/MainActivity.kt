package com.gujaratpost.app.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.gujaratpost.app.R
import com.gujaratpost.app.databinding.ActivityMainBinding
import com.gujaratpost.app.ui.category.CategoryFragment
import com.gujaratpost.app.ui.home.HomeFragment
import com.gujaratpost.app.ui.saved.SavedArticlesFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment by lazy { HomeFragment() }
    private val categoryFragment by lazy { CategoryFragment() }
    private val savedArticlesFragment by lazy { SavedArticlesFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GujaratPost)
        super.onCreate(savedInstanceState)
        com.gujaratpost.app.data.api.RetrofitClient.initFromPreferences(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(homeFragment)
        }

        setupTopToolbar()
        setupInlineSearch()
        setupNavigationDrawer()
    }

    private fun setupTopToolbar() {
        // Hamburger click opens the dark sidebar drawer
        binding.btnHamburger.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Open inline search bar in-place (NO popups!)
        binding.btnSearch.setOnClickListener {
            openInlineSearch()
        }
    }

    private fun setupInlineSearch() {
        binding.btnCloseSearch.setOnClickListener {
            closeInlineSearch()
        }

        binding.btnExecuteSearch.setOnClickListener {
            executeSearch()
        }

        binding.etInlineSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                executeSearch()
                true
            } else {
                false
            }
        }
    }

    private fun openInlineSearch() {
        binding.layoutNormalHeader.visibility = View.GONE
        binding.layoutSearchInline.visibility = View.VISIBLE
        binding.etInlineSearch.requestFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(binding.etInlineSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun closeInlineSearch() {
        binding.etInlineSearch.text.clear()
        binding.layoutSearchInline.visibility = View.GONE
        binding.layoutNormalHeader.visibility = View.VISIBLE

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.etInlineSearch.windowToken, 0)

        // Reset home filter and drawer highlight
        highlightDrawerCategoryBySlug("all")
        replaceFragment(homeFragment)
        homeFragment.filterByCategory("all", "બધા")
    }

    private fun executeSearch() {
        val query = binding.etInlineSearch.text.toString().trim()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.etInlineSearch.windowToken, 0)

        if (query.isNotBlank()) {
            replaceFragment(homeFragment)
            homeFragment.filterByCategory(null, "શોધ: $query")
        }
    }

    private val drawerMenuItems by lazy {
        listOf(
            binding.navDrawer.menuDrawerHome,
            binding.navDrawer.menuDrawerGujarat,
            binding.navDrawer.menuDrawerNational,
            binding.navDrawer.menuDrawerWorld,
            binding.navDrawer.menuDrawerEntertainment,
            binding.navDrawer.menuDrawerPopular,
            binding.navDrawer.menuDrawerGallery,
            binding.navDrawer.menuDrawerPolitics,
            binding.navDrawer.menuDrawerFactcheck
        )
    }

    private fun selectDrawerCategory(selectedView: android.widget.TextView, slug: String, nameGu: String) {
        highlightDrawerItem(selectedView)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        replaceFragment(homeFragment)
        homeFragment.filterByCategory(slug, nameGu)
    }

    private fun highlightDrawerItem(targetView: android.widget.TextView?) {
        for (item in drawerMenuItems) {
            if (item == targetView) {
                // RED active text and bold font
                item.setTextColor(android.graphics.Color.parseColor("#E53935"))
                item.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                // Clean white inactive text
                item.setTextColor(android.graphics.Color.WHITE)
                item.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    /**
     * Programmatically syncs the drawer highlight when category is selected from HomeFragment
     */
    fun highlightDrawerCategoryBySlug(slug: String?) {
        val targetSlug = if (slug.isNullOrBlank() || slug == "all") "all" else slug.lowercase().trim()
        val targetView = when {
            targetSlug == "all" -> binding.navDrawer.menuDrawerHome
            targetSlug.contains("gujarat") -> binding.navDrawer.menuDrawerGujarat
            targetSlug.contains("national") || targetSlug.contains("bharat") -> binding.navDrawer.menuDrawerNational
            targetSlug.contains("world") || targetSlug.contains("international") -> binding.navDrawer.menuDrawerWorld
            targetSlug.contains("entertainment") -> binding.navDrawer.menuDrawerEntertainment
            targetSlug.contains("popular") || targetSlug.contains("trending") -> binding.navDrawer.menuDrawerPopular
            targetSlug.contains("gallery") || targetSlug.contains("video") -> binding.navDrawer.menuDrawerGallery
            targetSlug.contains("politics") -> binding.navDrawer.menuDrawerPolitics
            targetSlug.contains("fact") -> binding.navDrawer.menuDrawerFactcheck
            else -> null
        }
        highlightDrawerItem(targetView)
    }

    private fun setupNavigationDrawer() {
        binding.navDrawer.btnCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Home
        binding.navDrawer.menuDrawerHome.setOnClickListener {
            selectDrawerCategory(binding.navDrawer.menuDrawerHome, "all", "બધા")
        }

        // Gujarat
        binding.navDrawer.menuDrawerGujarat.setOnClickListener {
            selectDrawerCategory(binding.navDrawer.menuDrawerGujarat, "gujarat", "ગુજરાત")
        }

        // National
        binding.navDrawer.menuDrawerNational.setOnClickListener {
            selectDrawerCategory(binding.navDrawer.menuDrawerNational, "national", "રાષ્ટ્રીય")
        }

        // World
        binding.navDrawer.menuDrawerWorld.setOnClickListener {
            selectDrawerCategory(binding.navDrawer.menuDrawerWorld, "world", "વિશ્વ")
        }

        // Entertainment
        binding.navDrawer.menuDrawerEntertainment.setOnClickListener {
            selectDrawerCategory(binding.navDrawer.menuDrawerEntertainment, "entertainment", "મનોરંજન")
        }

        // Popular Stories
        binding.navDrawer.menuDrawerPopular.setOnClickListener {
            selectDrawerCategory(binding.navDrawer.menuDrawerPopular, "trending", "લોકપ્રિય વાર્તાઓ")
        }

        // Gallery & Videos
        binding.navDrawer.menuDrawerGallery.setOnClickListener {
            selectDrawerCategory(binding.navDrawer.menuDrawerGallery, "videos", "વીડિયો & ગેલેરી")
        }

        // Politics
        binding.navDrawer.menuDrawerPolitics.setOnClickListener {
            selectDrawerCategory(binding.navDrawer.menuDrawerPolitics, "politics", "રાજકારણ")
        }

        // Fact Check
        binding.navDrawer.menuDrawerFactcheck.setOnClickListener {
            selectDrawerCategory(binding.navDrawer.menuDrawerFactcheck, "factcheck", "ફેક્ટ ચેક")
        }

        // Saved Articles
        binding.navDrawer.menuDrawerSaved.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(savedArticlesFragment)
        }

        // Settings / Server Configuration
        binding.navDrawer.menuDrawerSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            showServerSettingsDialog()
        }
    }

    private fun showServerSettingsDialog() {
        val servers = arrayOf(
            "🏠 Local Wi-Fi PC (10.110.59.96:5000)",
            "📱 Android Emulator (10.0.2.2:5000)",
            "☁️ Cloud Production (gujaratpost.vercel.app)",
            "✏️ Custom Server IP / URL"
        )
        val serverUrls = arrayOf(
            com.gujaratpost.app.utils.Constants.LOCAL_WIFI_URL,
            com.gujaratpost.app.utils.Constants.LOCAL_EMULATOR_URL,
            com.gujaratpost.app.utils.Constants.CLOUD_PRODUCTION_URL,
            ""
        )

        var selectedIndex = 0
        val current = com.gujaratpost.app.data.api.RetrofitClient.activeApiBaseUrl
        for (i in serverUrls.indices) {
            if (serverUrls[i].isNotBlank() && current.contains(serverUrls[i].removeSuffix("/"))) {
                selectedIndex = i
                break
            }
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("API સર્વર સેટિંગ્સ (Server Settings)")
            .setSingleChoiceItems(servers, selectedIndex) { dialog, which ->
                dialog.dismiss()
                if (which == 3) {
                    showCustomServerInputDialog()
                } else {
                    val chosen = serverUrls[which]
                    com.gujaratpost.app.data.api.RetrofitClient.updateBaseUrl(chosen, this)
                    android.widget.Toast.makeText(this, "સર્વર કનેક્ટ કર્યું: $chosen", android.widget.Toast.LENGTH_SHORT).show()
                    homeFragment.reloadCurrentFeed()
                }
            }
            .setNegativeButton("બંધ કરો (Close)", null)
            .show()
    }

    private fun showCustomServerInputDialog() {
        val input = android.widget.EditText(this).apply {
            hint = "http://192.168.1.XX:5000/"
            setText(com.gujaratpost.app.data.api.RetrofitClient.activeApiBaseUrl)
            setSelection(text.length)
        }
        val container = android.widget.FrameLayout(this).apply {
            setPadding(48, 24, 48, 24)
            addView(input)
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Custom Server URL")
            .setView(container)
            .setPositiveButton("સેવ કરો (Save)") { _, _ ->
                val entered = input.text.toString().trim()
                if (entered.isNotBlank()) {
                    com.gujaratpost.app.data.api.RetrofitClient.updateBaseUrl(entered, this)
                    android.widget.Toast.makeText(this, "સર્વર અપડેટ થયું: $entered", android.widget.Toast.LENGTH_SHORT).show()
                    homeFragment.reloadCurrentFeed()
                }
            }
            .setNegativeButton("રદ કરો (Cancel)", null)
            .show()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.layoutSearchInline.visibility == View.VISIBLE) {
            closeInlineSearch()
        } else if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
