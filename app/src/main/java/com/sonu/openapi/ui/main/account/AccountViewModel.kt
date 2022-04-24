package com.sonu.openapi.ui.main.account

import android.util.Log
import androidx.lifecycle.LiveData
import com.sonu.openapi.models.AccountProperties
import com.sonu.openapi.repository.main.AccountRepository
import com.sonu.openapi.session.SessionManager
import com.sonu.openapi.ui.BaseViewModel
import com.sonu.openapi.ui.DataState
import com.sonu.openapi.ui.Loading
import com.sonu.openapi.ui.main.account.state.AccountStateEvent
import com.sonu.openapi.ui.main.account.state.AccountStateEvent.*
import com.sonu.openapi.ui.main.account.state.AccountViewState
import com.sonu.openapi.util.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    private val accountRepository: AccountRepository
) : BaseViewModel<AccountStateEvent, AccountViewState>() {
    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>>  {
         when (stateEvent) {
            is GetAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.getAccountProperties(authToken)
                } ?: AbsentLiveData.create()
            }
            is UpdateAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_pk?.let { pk ->
                        val newAccountProperties = AccountProperties(
                            pk,
                            stateEvent.email,
                            stateEvent.username
                        )
                        accountRepository.saveAccountProperties(
                            authToken,
                            newAccountProperties
                        )
                    }
                } ?: AbsentLiveData.create()
            }
            is ChangePasswordEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.updatePassword(
                        authToken,
                        stateEvent.currentPassword,
                        stateEvent.newPassword,
                        stateEvent.confirmNewPassword
                    )
                } ?: AbsentLiveData.create()
            }
            is None -> {
                return object: LiveData<DataState<AccountViewState>>(){
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }
            }
        }
    }

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties == accountProperties) {
            return
        }
        update.accountProperties = accountProperties
        _viewState.value = update
    }

    fun logout() {
        sessionManager.logout()
    }

    fun cancelActiveJobs() {
        Log.d(TAG, "cancelActiveJobs: Account viewmodel cancelling all active jobs")
        accountRepository.cancelActiveJobs()  // cancel active jobs
        handlePendingData() // hide progress bar bec if  user press back button and progress bar is showing and job is cancelled so hide the progressbar
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}