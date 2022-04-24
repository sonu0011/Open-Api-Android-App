package com.sonu.openapi.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.sonu.openapi.api.auth.OpenApiAuthService
import com.sonu.openapi.api.auth.network_responses.LoginResponse
import com.sonu.openapi.api.auth.network_responses.RegistrationResponse
import com.sonu.openapi.models.AccountProperties
import com.sonu.openapi.models.AuthToken
import com.sonu.openapi.persistence.AccountPropertiesDao
import com.sonu.openapi.persistence.AuthTokenDao
import com.sonu.openapi.repository.JobManager
import com.sonu.openapi.repository.NetworkBoundResource
import com.sonu.openapi.session.SessionManager
import com.sonu.openapi.ui.DataState
import com.sonu.openapi.ui.Response
import com.sonu.openapi.ui.ResponseType
import com.sonu.openapi.ui.auth.state.AuthViewState
import com.sonu.openapi.ui.auth.state.LoginFields
import com.sonu.openapi.ui.auth.state.RegistrationFields
import com.sonu.openapi.util.AbsentLiveData
import com.sonu.openapi.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.sonu.openapi.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.sonu.openapi.util.GenericApiResponse
import com.sonu.openapi.util.GenericApiResponse.*
import com.sonu.openapi.util.PreferenceKeys
import com.sonu.openapi.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.Job
import javax.inject.Inject


class AuthRepository @Inject constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    private val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : JobManager("AuthRepository") {
    private val TAG = "AppDebug"


    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if (loginFieldErrors != LoginFields.LoginError.none()) {
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog)
        }
        return object : NetworkBoundResource<LoginResponse, Any, AuthViewState>(
            isNetworkAvailable = sessionManager.isNetworkAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            //not used in this case
            override fun loadFromCache(): LiveData<AuthViewState> = AbsentLiveData.create()

            //not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {}

            //not used in this case
            override suspend fun createCacheRequestAndReturn() {}
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: $response")
                // Incorrect login credentials counts as a 200 response from server, so need to handle that
                if (response.body.response == GENERIC_AUTH_ERROR) {
                    return onErrorReturn(
                        response.body.errorMessage,
                        shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }

                // Don't care about result here. Just insert if it doesn't exist b/c of foreign key relationship
                // with AuthToken
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )
                // will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog)
                        )
                    )
                }
                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(
                                response.body.pk,
                                response.body.token
                            )
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return openApiAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
              addJob(methodName ="attemptLogin" , job = job)
            }
        }.asLiveData()
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {

        val registrationFieldErrors =
            RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if (registrationFieldErrors != RegistrationFields.RegistrationError.none()) {
            return returnErrorResponse(registrationFieldErrors, ResponseType.Dialog)
        }

        return object : NetworkBoundResource<RegistrationResponse, Any, AuthViewState>(
            sessionManager.isNetworkAvailable(),
            isNetworkRequest = true,
            shouldLoadFromCache = false,
            shouldCancelIfNoInternet = true
        ) {
            //not used in this case
            override fun loadFromCache(): LiveData<AuthViewState> = AbsentLiveData.create()

            //not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {}

            //not used in this case
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: ${response}")
                if (response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                // Don't care about result here. Just insert if it doesn't exist b/c of foreign key relationship
                // with AuthToken
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )
                // will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog)
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return openApiAuthService.register(email, username, password, confirmPassword)
            }

            override fun setJob(job: Job) {
                addJob(methodName ="attemptRegistration" , job = job)

            }

        }.asLiveData()
    }


    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)
        return if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found.")
            returnNoTokenFound()
        } else {
            object : NetworkBoundResource<Void, Any, AuthViewState>(
                sessionManager.isNetworkAvailable(),
                isNetworkRequest = false,
                shouldLoadFromCache = false,
                shouldCancelIfNoInternet = false
            ) {
                //not used in this case
                override fun loadFromCache(): LiveData<AuthViewState> = AbsentLiveData.create()

                //not used in this case
                override suspend fun updateLocalDb(cacheObject: Any?) {}

                //not used in this case
                override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {}

                //not used in this case
                override fun createCall(): LiveData<GenericApiResponse<Void>> =
                    AbsentLiveData.create()

                override suspend fun createCacheRequestAndReturn() {
                    accountPropertiesDao.searchByEmail(previousAuthUserEmail)
                        ?.let { accountProperties ->
                            Log.d(
                                TAG,
                                "createCacheRequestAndReturn: searching for token... account properties: ${accountProperties}"
                            )
                            accountProperties.let {
                                if (accountProperties.pk > -1) {
                                    authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                        if (authToken != null) {
                                            if (authToken.token != null) {
                                                onCompleteJob(
                                                    DataState.data(
                                                        AuthViewState(authToken = authToken)
                                                    )
                                                )
                                                return
                                            }
                                        }
                                    }
                                }
                            }
                            Log.d(TAG, "createCacheRequestAndReturn: AuthToken not found...")
                            onCompleteJob(
                                DataState.data(
                                    null,
                                    Response(
                                        RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                        ResponseType.None
                                    )
                                )
                            )

                        }
                    Log.e(TAG, "createCacheRequestAndReturn: AuthToken not found...")
                }

                override fun setJob(job: Job) {
                    addJob(methodName = "checkPreviousAuthUser" , job = job)
                }
            }.asLiveData()

        }

    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    null,
                    Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None)
                )
            }
        }
    }


    private fun returnErrorResponse(
        errorMessage: String,
        responseType: ResponseType
    ): LiveData<DataState<AuthViewState>> {
        Log.e(TAG, "returnErrorResponse: $errorMessage")
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    response = Response(
                        message = errorMessage,
                        responseType = responseType
                    )
                )
            }

        }
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }
}