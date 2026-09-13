package com.gujaratpost.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gujaratpost.app.R
import com.gujaratpost.app.databinding.ActivityMainBinding
import com.gujaratpost.app.ui.category.CategoryFragment
import com.gujaratpost.app.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment by lazy { HomeFragment() }
    private val categoryFragment by lazy { CategoryFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(homeFragment)
        }

        setupBottomNavigation()
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
                    // Navigate to Home tab and trigger breaking filter
                    replaceFragment(homeFragment)
                    true
                }
                R.id.nav_saved -> {
                    // For saved bookmarks, open categories/favorites
                    replaceFragment(categoryFragment)
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
}
