package com.sonu.openapi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.sonu.openapi.R
import com.sonu.openapi.ui.BaseActivity
import com.sonu.openapi.ui.auth.state.AuthStateEvent
import com.sonu.openapi.ui.main.MainActivity
import com.sonu.openapi.ui.showOrHideProgressBar
import com.sonu.openapi.viewmodels.ViewModelProviderFactory
import javax.inject.Inject

class AuthActivity : BaseActivity() {
    @Inject
    lateinit var providerFactory: ViewModelProviderFactory
    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)
        subscribeObservers()

    }

    override fun onResume() {
        super.onResume()
        checkPreviousAuthUser()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(this) { dataState ->
            onDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let {
                        it.authToken?.let { authToken ->
                            viewModel.setAuthToken(authToken)
                        }
                    }
                }
            }
        }

        viewModel.viewState.observe(this) { authState ->
            authState.authToken?.let {
                sessionManager.login(it)
            }
        }

        sessionManager.cachedToken.observe(this) { authoken ->
            if (authoken?.account_pk != null && authoken.token != null) {
                navMainActivity()
            }

        }
    }

    private fun checkPreviousAuthUser() {
        viewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthEvent)
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun displayProgressBar(bool: Boolean) {
        findViewById<ProgressBar>(R.id.progress_bar).showOrHideProgressBar(bool)
    }

    override fun expandAppBar() {
        //not applicable
    }
}