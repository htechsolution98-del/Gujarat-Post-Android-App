package com.gujaratpost.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        setupNavigationDrawer()
        setupBottomNavigation()
    }

    private fun setupTopToolbar() {
        // Hamburger click opens the dark sidebar drawer
        binding.btnHamburger.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Search action
        binding.btnSearch.setOnClickListener {
            showSearchDialog()
        }

        // Overflow 3-dots action
        binding.btnOverflowMenu.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun setupNavigationDrawer() {
        binding.navDrawer.btnCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Home
        binding.navDrawer.menuDrawerHome.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            homeFragment.filterByCategory("all", "બધા")
        }

        // Gujarat
        binding.navDrawer.menuDrawerGujarat.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            homeFragment.filterByCategory("gujarat", "ગુજરાત")
        }

        // National
        binding.navDrawer.menuDrawerNational.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            homeFragment.filterByCategory("national", "રાષ્ટ્રીય")
        }

        // World
        binding.navDrawer.menuDrawerWorld.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            homeFragment.filterByCategory("world", "વિશ્વ")
        }

        // Entertainment
        binding.navDrawer.menuDrawerEntertainment.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            homeFragment.filterByCategory("entertainment", "મનોરંજન")
        }

        // Popular Stories
        binding.navDrawer.menuDrawerPopular.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            homeFragment.filterByCategory("trending", "લોકપ્રિય વાર્તાઓ")
        }

        // Gallery & Videos
        binding.navDrawer.menuDrawerGallery.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            homeFragment.filterByCategory("videos", "વીડિયો & ગેલેરી")
        }

        // Politics
        binding.navDrawer.menuDrawerPolitics.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            homeFragment.filterByCategory("politics", "રાજકારણ")
        }

        // Fact Check
        binding.navDrawer.menuDrawerFactcheck.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(homeFragment)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            homeFragment.filterByCategory("factcheck", "ફેક્ટ ચેક")
        }

        // Saved Articles
        binding.navDrawer.menuDrawerSaved.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNavigation.selectedItemId = R.id.nav_saved
        }

        // Settings / About
        binding.navDrawer.menuDrawerSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            showAboutDialog()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(homeFragment)
                    true
                }
                R.id.nav_categories -> {
                    replaceFragment(categoryFragment)
                    true
                }
                R.id.nav_breaking -> {
                    replaceFragment(homeFragment)
                    homeFragment.filterByCategory("breaking", "બ્રેકિંગ")
                    true
                }
                R.id.nav_saved -> {
                    replaceFragment(savedArticlesFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showSearchDialog() {
        val input = android.widget.EditText(this).apply {
            hint = "સમાચાર શોધો... (Search news)"
            setPadding(40, 30, 40, 30)
        }
        AlertDialog.Builder(this)
            .setTitle("સમાચાર શોધો")
            .setView(input)
            .setPositiveButton("શોધો") { _, _ ->
                val query = input.text.toString().trim()
                if (query.isNotBlank()) {
                    replaceFragment(homeFragment)
                    homeFragment.filterByCategory(null, "શોધ: $query")
                }
            }
            .setNegativeButton("રદ કરો", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("ગુજરાત પોસ્ટ (Gujarat Post)")
            .setMessage("સત્ય અને સચોટ સમાચાર.\n\nઆવૃત્તિ: 1.0.0 (Production Live)\nબેકએન્ડ: Live Render API & MySQL\n© 2026 ગુજરાત પોસ્ટ મીડિયા નેટવર્ક.")
            .setPositiveButton("ઠીક છે", null)
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
