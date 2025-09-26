package com.elber.parts

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
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

        // Let NavigationUI manage clicks, selection, back stacks
        binding.bottomNav.setupWithNavController(navController)

        // Show bottom bar & FAB only on top-level destinations (incl. cart)
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val show = dest.id in setOf(
                R.id.homeFragment,
                R.id.categoryFragment,
                R.id.searchFragment,
                R.id.profileFragment,
                R.id.cartFragment
            )
            binding.bottomNav.visibility = if (show) View.VISIBLE else View.GONE
            binding.fabCart.visibility  = if (show) View.VISIBLE else View.GONE
        }

        // ✅ FAB: just select the cart tab (behaves exactly like tapping the center item)
        binding.fabCart.setOnClickListener {
            binding.bottomNav.selectedItemId = R.id.cartFragment
        }
    }
}
