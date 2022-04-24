package com.sonu.openapi.ui.main.blog.state.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.bumptech.glide.RequestManager
import com.sonu.openapi.models.BlogPost
import com.sonu.openapi.persistence.BlogQueryUtils
import com.sonu.openapi.repository.main.BlogRepository
import com.sonu.openapi.session.SessionManager
import com.sonu.openapi.ui.BaseViewModel
import com.sonu.openapi.ui.DataState
import com.sonu.openapi.ui.Loading
import com.sonu.openapi.ui.main.blog.state.BlogStateEvent
import com.sonu.openapi.ui.main.blog.state.BlogStateEvent.*
import com.sonu.openapi.ui.main.blog.state.BlogViewState
import com.sonu.openapi.util.AbsentLiveData
import com.sonu.openapi.util.PreferenceKeys.Companion.BLOG_FILTER
import com.sonu.openapi.util.PreferenceKeys.Companion.BLOG_ORDER
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) : BaseViewModel<BlogStateEvent, BlogViewState>() {

    init {
        setBlogFilter(
            sharedPreferences.getString(
                BLOG_FILTER,
                BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
            )
        )
        sharedPreferences.getString(
            BLOG_ORDER,
            BlogQueryUtils.BLOG_ORDER_ASC
        )?.let {
            setBlogOrder(
                it
            )
        }
    }

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        return when (stateEvent) {

            is BlogSearchEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                        authToken,
                        getSearchQuery(),
                        getPage(),
                        filterAndOrder = getOrder() + getFilter(),
                    )
                } ?: AbsentLiveData.create()
            }

            is DeleteBlogPostEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.deleteBlogPost(
                        authToken = authToken,
                        blogPost = getBlogPost()
                    )
                } ?: AbsentLiveData.create()
            }


            is CheckAuthorOfBlogPost -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.isAuthorOfBlogPost(
                        authToken = authToken,
                        slug = getSlug()
                    )
                } ?: AbsentLiveData.create()
            }
            is UpdateBlogPostEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->

                    val title = stateEvent.title.toRequestBody(
                        "text/plain".toMediaTypeOrNull(),
                    )
                    val body = stateEvent.body.toRequestBody(
                        "text/plain".toMediaTypeOrNull(),
                    )
                    blogRepository.updateBlogPost(
                        authToken = authToken,
                        slug = getSlug(),
                        title = title,
                        body = body,
                        image = stateEvent.image
                    )
                } ?: AbsentLiveData.create()

            }
            is None -> {
                object : LiveData<DataState<BlogViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            loading = Loading(false)
                        )
                    }
                }
            }
        }
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter)
        editor.apply()

        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }


    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    private fun cancelActiveJobs() {
        blogRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}











