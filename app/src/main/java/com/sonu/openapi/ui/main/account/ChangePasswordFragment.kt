package com.sonu.openapi.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.sonu.openapi.R
import com.sonu.openapi.databinding.FragmentAccountBinding
import com.sonu.openapi.databinding.FragmentChangePasswordBinding
import com.sonu.openapi.ui.main.account.state.AccountStateEvent
import com.sonu.openapi.util.SuccessHandling.Companion.RESPONSE_PASSWORD_UPDATE_SUCCESS

class ChangePasswordFragment : BaseAccountFragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.updatePasswordButton.setOnClickListener {
            binding.apply {
                viewModel.setStateEvent(
                    AccountStateEvent.ChangePasswordEvent(
                        inputCurrentPassword.text.toString(),
                        inputNewPassword.text.toString(),
                        inputConfirmNewPassword.text.toString(),
                    )
                )
            }
        }
        subscribeObservers()

    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.onDataStateChange(dataState)
            Log.d(TAG, "ChangePasswordFragment, DataState: ${dataState}")
            if (dataState != null) {
                dataState.data?.let { data ->
                    data.response?.let { event ->
                        if (event.peekContent()   //peek content for moving back because in baseActivity event was consumed and no way to get the event data again
                                .message
                                .equals(RESPONSE_PASSWORD_UPDATE_SUCCESS)
                        ) {
                            stateChangeListener.hideSoftKeyboard()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })
    }

}