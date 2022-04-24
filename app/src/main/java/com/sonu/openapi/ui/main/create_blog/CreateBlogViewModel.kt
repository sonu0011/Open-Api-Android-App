package com.sonu.openapi.ui.main.create_blog

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.sonu.openapi.repository.main.CreateBlogRepository
import com.sonu.openapi.session.SessionManager
import com.sonu.openapi.ui.BaseViewModel
import com.sonu.openapi.ui.DataState
import com.sonu.openapi.ui.Loading
import com.sonu.openapi.ui.main.create_blog.state.CreateBlogStateEvent
import com.sonu.openapi.ui.main.create_blog.state.CreateBlogStateEvent.CreateNewBlogEvent
import com.sonu.openapi.ui.main.create_blog.state.CreateBlogStateEvent.None
import com.sonu.openapi.ui.main.create_blog.state.CreateBlogViewState
import com.sonu.openapi.util.AbsentLiveData
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class CreateBlogViewModel
@Inject
constructor(
    private val createBlogRepository: CreateBlogRepository,
    val sessionManager: SessionManager
) : BaseViewModel<CreateBlogStateEvent, CreateBlogViewState>() {

    override fun handleStateEvent(
        stateEvent: CreateBlogStateEvent
    ): LiveData<DataState<CreateBlogViewState>> {

        when (stateEvent) {

            is CreateNewBlogEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    val title = stateEvent.title.toRequestBody("text/plain".toMediaTypeOrNull())
                    val body = stateEvent.body.toRequestBody("text/plain".toMediaTypeOrNull())

                    createBlogRepository.createNewBlogPost(
                        authToken,
                        title,
                        body,
                        stateEvent.image
                    )
                } ?: AbsentLiveData.create()
            }

            is None -> {
                return liveData {
                    emit(
                        DataState(
                            null,
                            Loading(false),
                            null
                        )
                    )
                }
            }
        }
    }

    override fun initNewViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    fun setNewBlogFields(title: String?, body: String?, uri: Uri?) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        title?.let { newBlogFields.newBlogTitle = it }
        body?.let { newBlogFields.newBlogBody = it }
        uri?.let { newBlogFields.newImageUri = it }
        update.blogFields = newBlogFields
        _viewState.value = update
    }

    fun clearNewBlogFields() {
        val update = getCurrentViewStateOrNew()
        update.blogFields = CreateBlogViewState.NewBlogFields()
        setViewState(update)
    }

    fun cancelActiveJobs() {
        createBlogRepository.cancelActiveJobs()
        handlePendingData()
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}











