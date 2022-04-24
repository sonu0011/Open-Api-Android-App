package com.sonu.openapi.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.sonu.openapi.api.GenericResponse
import com.sonu.openapi.api.main.OpenApiMainService
import com.sonu.openapi.models.AccountProperties
import com.sonu.openapi.models.AuthToken
import com.sonu.openapi.persistence.AccountPropertiesDao
import com.sonu.openapi.repository.JobManager
import com.sonu.openapi.repository.NetworkBoundResource
import com.sonu.openapi.session.SessionManager
import com.sonu.openapi.ui.DataState
import com.sonu.openapi.ui.Response
import com.sonu.openapi.ui.ResponseType
import com.sonu.openapi.ui.main.account.state.AccountViewState
import com.sonu.openapi.util.AbsentLiveData
import com.sonu.openapi.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject


class AccountRepository @Inject constructor(
    private val accountPropertiesDao: AccountPropertiesDao,
    private val openApiMainService: OpenApiMainService,
    private val sessionManager: SessionManager
) : JobManager("AccountRepository") {

    private val TAG: String = "AppDebug"

    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                isNetworkAvailable = sessionManager.isNetworkAvailable(),
                isNetworkRequest = true,
                shouldLoadFromCache = true,
                shouldCancelIfNoInternet = false
            ) {
            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)
                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return openApiMainService.getAccountProperties(
                    authorization = "Token ${authToken.token!!}"
                )
            }

            // used when network successfully fetched the updated data
            override suspend fun createCacheRequestAndReturn() {
                //you have to perform addSource on Main context because you can't add forever observer on background thread
                withContext(Main) {
                    val dbSource = loadFromCache()
                    result.addSource(dbSource) { viewState ->
                        result.removeSource(dbSource)
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }

            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                    .switchMap { accountProperties ->
                        object : LiveData<AccountViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(
                                    accountProperties = accountProperties
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cacheObject.pk,
                        cacheObject.email,
                        cacheObject.username
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob(methodName = "getAccountProperties", job = job)
            }
        }.asLiveData()
    }

    fun saveAccountProperties(
        authToken: AuthToken,
        accountProperties: AccountProperties
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isNetworkAvailable(),
            true,
            false,
            true
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {
            }

            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null) // The update does not return a CacheObject

                withContext(Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                message = response.body.msg ?: response.body.response,
                                ResponseType.Toast
                            )
                        )
                    )
                }
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.saveAccountProperties(
                    "Token ${authToken.token!!}",
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                addJob(methodName = "saveAccountProperties", job = job)
            }

        }.asLiveData()
    }

    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isNetworkAvailable(),
            true,
            false,
            shouldCancelIfNoInternet = true
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<GenericResponse>) {
                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(
                                message = response.body.msg ?: response.body.response,
                                ResponseType.Toast
                            )
                        )
                    )
                }
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.updatePassword(
                    "Token ${authToken.token!!}",
                    currentPassword,
                    newPassword,
                    confirmNewPassword
                )
            }

            // not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {
            }

            override fun setJob(job: Job) {
                addJob(methodName = "updatePassword", job = job)

            }
        }.asLiveData()
    }
}