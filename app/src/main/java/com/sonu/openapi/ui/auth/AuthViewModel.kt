package com.sonu.openapi.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import com.sonu.openapi.models.AuthToken
import com.sonu.openapi.repository.auth.AuthRepository
import com.sonu.openapi.ui.BaseViewModel
import com.sonu.openapi.ui.DataState
import com.sonu.openapi.ui.Loading
import com.sonu.openapi.ui.auth.state.AuthStateEvent
import com.sonu.openapi.ui.auth.state.AuthStateEvent.*
import com.sonu.openapi.ui.auth.state.AuthViewState
import com.sonu.openapi.ui.auth.state.LoginFields
import com.sonu.openapi.ui.auth.state.RegistrationFields
import com.sonu.openapi.ui.main.account.state.AccountStateEvent
import com.sonu.openapi.ui.main.account.state.AccountViewState
import com.sonu.openapi.util.AbsentLiveData
import javax.inject.Inject


class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthStateEvent, AuthViewState>() {

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        return when (stateEvent) {
            is CheckPreviousAuthEvent -> {
                authRepository.checkPreviousAuthUser()
            }
            is LoginAttemptEvent -> {
                authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }
            is RegisterAttemptEvent -> {
                authRepository.attemptRegistration(
                    email = stateEvent.email,
                    password = stateEvent.password,
                    confirmPassword = stateEvent.confirm_password,
                    username = stateEvent.username
                )
            }
            None -> {
                return object : LiveData<DataState<AuthViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }
            }
        }
    }

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields = registrationFields
        setViewState(update)
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        setViewState(update)
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        setViewState(update)
    }

    fun cancelActiveJobs() {
        Log.d(TAG, "cancelActiveJobs: AuthViewModel clearing jobs...")
        authRepository.cancelActiveJobs()
        handlePendingData() // hide progress bar bec if  user press back button and progress bar is showing and job is cancelled so hide the progressbar
    }

    private fun handlePendingData() {
        setStateEvent(None)
    }


    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}