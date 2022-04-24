package com.sonu.openapi.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.sonu.openapi.R
import com.sonu.openapi.databinding.ActivityMainBinding
import com.sonu.openapi.ui.BaseActivity
import com.sonu.openapi.ui.auth.AuthActivity
import com.sonu.openapi.ui.showOrHideProgressBar

class MainActivity : BaseActivity(), NavController.OnDestinationChangedListener {
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragments_container) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener(this)

        setupActionBarAndBottomNavigationView()

        subscribeObservers()
    }

    private fun setupActionBarAndBottomNavigationView() {
        setSupportActionBar(binding.toolBar)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.blogFragment, R.id.createBlogFragment, R.id.accountFragment)
        )
        binding.toolBar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    private fun subscribeObservers() {
        sessionManager.cachedToken.observe(this) { authToken ->

            if (authToken == null || authToken.account_pk == -1 || authToken.token == null) {
                navAuthActivity()
            }
        }
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun displayProgressBar(bool: Boolean) {
        binding.progressBar.showOrHideProgressBar(bool)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        expandAppBar()
    }

    override fun expandAppBar() {
        binding.appBar.setExpanded(true, false)
    }
}