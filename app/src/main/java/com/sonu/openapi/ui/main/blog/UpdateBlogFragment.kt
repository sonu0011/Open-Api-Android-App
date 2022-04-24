package com.sonu.openapi.ui.main.blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.navigation.fragment.findNavController
import com.sonu.openapi.R
import com.sonu.openapi.databinding.FragmentUpdateBlogBinding
import com.sonu.openapi.ui.*
import com.sonu.openapi.ui.main.blog.state.BlogStateEvent
import com.sonu.openapi.ui.main.blog.state.viewmodel.getUpdatedBlogUri
import com.sonu.openapi.ui.main.blog.state.viewmodel.onBlogPostUpdateSuccess
import com.sonu.openapi.ui.main.blog.state.viewmodel.setUpdatedBlogFields
import com.sonu.openapi.util.Constants.Companion.GALLERY_REQUEST_CODE
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UpdateBlogFragment : BaseBlogFragment() {
    private var _binding: FragmentUpdateBlogBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateBlogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        binding.imageContainer.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }
    }

    private fun pickFromGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun showImageSelectionError() {
        stateChangeListener.onDataStateChange(
            DataState(
                Event(
                    StateError(
                        Response(
                            "Something went wrong with the image.",
                            ResponseType.Dialog
                        )
                    )
                ),
                Loading(isLoading = false),
                Data(Event.dataEvent(null), null)
            )
        )
    }


    fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner) { dataState ->
            stateChangeListener.onDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.getContentIfNotHandled()?.let { viewState ->
                    // if this is not null, the blogpost was updated
                    viewState.viewBlogFields.blogPost?.let { blogPost ->
                        viewModel.onBlogPostUpdateSuccess(blogPost).let {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }

        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            viewState.updatedBlogFields.let { updatedBlogFields ->
                setBlogProperties(
                    updatedBlogFields.updatedBlogTitle,
                    updatedBlogFields.updatedBlogBody,
                    updatedBlogFields.updatedImageUri
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        activity?.let {
                            Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE: uri: $uri")
                            viewModel.setUpdatedBlogFields(
                                title = null,
                                body = null,
                                uri = uri
                            )
                        }
                    } ?: showImageSelectionError()
                }
            }
        }
    }


    private fun setBlogProperties(title: String?, body: String?, image: Uri?) {
        requestManager
            .load(image)
            .into(binding.blogImage)
        binding.blogTitle.setText(title)
        binding.blogBody.setText(body)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    private fun saveChanges() {
        var multipartBody: MultipartBody.Part? = null
        viewModel.getUpdatedBlogUri()?.let { imageUri ->
            imageUri.path?.let { filePath ->
                val imageFile = File(filePath)
                Log.d(TAG, "UpdateBlogFragment, imageFile: file: ${imageFile}")
                if (imageFile.exists()) {
                    val requestBody =
                        imageFile
                            .asRequestBody("image/*".toMediaTypeOrNull())
                    // name = field name in serializer
                    // filename = name of the image file
                    // requestBody = file with file type information
                    multipartBody = MultipartBody.Part.createFormData(
                        "image",
                        imageFile.name,
                        requestBody
                    )
                }
            }
        }
        viewModel.setStateEvent(
            BlogStateEvent.UpdateBlogPostEvent(
                binding.blogTitle.text.toString(),
                binding.blogBody.text.toString(),
                multipartBody
            )
        )
        stateChangeListener.hideSoftKeyboard()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setUpdatedBlogFields(
            title = binding.blogTitle.text.toString(),
            body = binding.blogBody.text.toString(),
            uri = null
        )
    }

}