package com.elber.parts

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.elber.parts.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment).navController

        // Default setup
        binding.bottomNav.setupWithNavController(navController)

        // ✅ Custom handling: ensure Home tab always goes back to HomeFragment
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    if (navController.currentDestination?.id != R.id.homeFragment) {
                        // Try to pop back to Home if it's already in the back stack
                        val popped = navController.popBackStack(R.id.homeFragment, false)
                        if (!popped) {
                            // If not in back stack, navigate fresh
                            navController.navigate(R.id.homeFragment)
                        }
                    }
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
            }
        }

        // Show bottom bar & FAB only on top-level destinations
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val show = dest.id in setOf(
                R.id.homeFragment,
                R.id.categoryFragment,
                R.id.searchFragment,
                R.id.profileFragment,
                R.id.cartFragment
            )
            binding.bottomNav.visibility = if (show) View.VISIBLE else View.GONE
            binding.fabCart.visibility = if (show) View.VISIBLE else View.GONE
        }

        // FAB: select the cart tab (like tapping the nav item)
        binding.fabCart.setOnClickListener {
            binding.bottomNav.selectedItemId = R.id.cartFragment
        }
    }
}
