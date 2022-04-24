package com.sonu.openapi.ui.main.blog

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.sonu.openapi.R
import com.sonu.openapi.databinding.FragmentViewBlogBinding
import com.sonu.openapi.models.BlogPost
import com.sonu.openapi.ui.AreYouSureCallback
import com.sonu.openapi.ui.UIMessage
import com.sonu.openapi.ui.UIMessageType
import com.sonu.openapi.ui.main.blog.state.BlogStateEvent
import com.sonu.openapi.ui.main.blog.state.viewmodel.*
import com.sonu.openapi.util.DateUtils
import com.sonu.openapi.util.SuccessHandling.Companion.SUCCESS_BLOG_DELETED

class ViewBlogFragment : BaseBlogFragment() {


    private var _binding: FragmentViewBlogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentViewBlogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        stateChangeListener.expandAppBar()
        binding.deleteButton.setOnClickListener {
            confirmDeleteRequest()
        }
    }

    private fun confirmDeleteRequest() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {
                deleteBlogPost()
            }

            override fun cancel() {
                // ignore
            }
        }
        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                getString(R.string.are_you_sure_delete),
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }

    private fun deleteBlogPost() {
        viewModel.setStateEvent(
            BlogStateEvent.DeleteBlogPostEvent()
        )
    }

    fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner) { dataState ->
            stateChangeListener.onDataStateChange(dataState)

            dataState.data?.let { data ->
                data.data?.getContentIfNotHandled()?.let { viewState ->
                    viewModel.setIsAuthorOfBlogPost(
                        viewState.viewBlogFields.isAuthorOfBlogPost
                    )
                }

                data.response?.peekContent()?.let { response ->
                    if (response.message.equals(SUCCESS_BLOG_DELETED)) {
                        viewModel.removeDeletedBlogPost()
                        findNavController().popBackStack()
                    }
                }


                viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
                    viewState.viewBlogFields.blogPost?.let { blogPost ->
                        setBlogProperties(blogPost)
                    }

                    if (viewState.viewBlogFields.isAuthorOfBlogPost) {
                        adaptViewToAuthorMode()
                    }
                }
            }
        }
    }


    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()  //invalidate the option menu so it recreates agin
        binding.deleteButton.visibility = View.VISIBLE
    }


    private fun setBlogProperties(blogPost: BlogPost) {
        binding.apply {

            requestManager
                .load(blogPost.image)
                .into(blogImage)
            blogTitle.text = blogPost.title
            blogAuthor.text = blogPost.username
            blogUpdateDate.setText(DateUtils.convertLongToStringDate(blogPost.date_updated))
            blogBody.text = blogPost.body
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (viewModel.isAuthorOfBlogPost()) {
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel.isAuthorOfBlogPost()) {   // although condition is not required but on safe side i am adding this condition
            when (item.itemId) {
                R.id.edit -> {
                    navUpdateBlogFragment()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navUpdateBlogFragment() {
        try {
            // prep for next fragment
            val blogPost = viewModel.getBlogPost()

            viewModel.setUpdatedBlogFields(
                blogPost.title,
                blogPost.body,
                blogPost.image.toUri()
            )
            val directions = ViewBlogFragmentDirections.actionViewBlogFragmentToUpdateBlogFragment()
            findNavController().navigate(directions)
        } catch (e: Exception) {
            // send error report or something. These fields should never be null. Not possible
            Log.e(TAG, "Exception: ${e.message}")
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}