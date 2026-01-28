package com.simplstudios.simplstream.presentation

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.simplstudios.simplstream.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Entry point for the TV app
 * Uses Leanback library for TV-optimized navigation
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupBackPressHandler()
    }
    
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if we're at the root of navigation (profile selection or browse)
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                val navController = navHostFragment?.navController
                
                // If we can't go back in the nav stack, show exit confirmation
                if (navController?.previousBackStackEntry == null) {
                    showExitConfirmationDialog()
                } else {
                    // Allow normal back navigation
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }
    
    private fun showExitConfirmationDialog() {
        ExitConfirmationDialog(
            onConfirm = { finish() },
            onCancel = { /* Do nothing, stay in app */ }
        ).show(supportFragmentManager, "exit_dialog")
    }
}
