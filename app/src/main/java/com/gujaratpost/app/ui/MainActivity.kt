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
        super.onCreate(savedInstanceState)
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

        // Reset home filter
        replaceFragment(homeFragment)
        homeFragment.filterByCategory("all")
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

    private fun setupNavigationDrawer() {
        binding.navDrawer.btnCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Home
        binding.navDrawer.menuDrawerHome.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            homeFragment.filterByCategory("all", "બધા")
        }

        // Gujarat
        binding.navDrawer.menuDrawerGujarat.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            homeFragment.filterByCategory("gujarat", "ગુજરાત")
        }

        // National
        binding.navDrawer.menuDrawerNational.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            homeFragment.filterByCategory("national", "રાષ્ટ્રીય")
        }

        // World
        binding.navDrawer.menuDrawerWorld.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            homeFragment.filterByCategory("world", "વિશ્વ")
        }

        // Entertainment
        binding.navDrawer.menuDrawerEntertainment.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            homeFragment.filterByCategory("entertainment", "મનોરંજન")
        }

        // Popular Stories
        binding.navDrawer.menuDrawerPopular.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            homeFragment.filterByCategory("trending", "લોકપ્રિય વાર્તાઓ")
        }

        // Gallery & Videos
        binding.navDrawer.menuDrawerGallery.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            homeFragment.filterByCategory("videos", "વીડિયો & ગેલેરી")
        }

        // Politics
        binding.navDrawer.menuDrawerPolitics.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            homeFragment.filterByCategory("politics", "રાજકારણ")
        }

        // Fact Check
        binding.navDrawer.menuDrawerFactcheck.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            homeFragment.filterByCategory("factcheck", "ફેક્ટ ચેક")
        }

        // Saved Articles
        binding.navDrawer.menuDrawerSaved.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(savedArticlesFragment)
        }

        // Settings / About: silently closes drawer, NO popups!
        binding.navDrawer.menuDrawerSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
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
