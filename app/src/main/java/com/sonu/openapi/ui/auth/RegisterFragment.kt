package com.sonu.openapi.ui.auth


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.sonu.openapi.databinding.FragmentRegisterBinding
import com.sonu.openapi.ui.auth.state.AuthStateEvent
import com.sonu.openapi.ui.auth.state.RegistrationFields

class RegisterFragment : BaseAuthFragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
        binding.registerButton.setOnClickListener {
            register()
        }
    }

    private fun register() {
        binding.apply {
            viewModel.setStateEvent(
                AuthStateEvent.RegisterAttemptEvent(
                    inputEmail.text.toString(),
                    inputUsername.text.toString(),
                    inputPassword.text.toString(),
                    inputPasswordConfirm.text.toString()
                )
            )
        }
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.registrationFields?.let {
                it.registration_email?.let { binding.inputEmail.setText(it) }
                it.registration_username?.let { binding.inputUsername.setText(it) }
                it.registration_password?.let { binding.inputPassword.setText(it) }
                it.registration_confirm_password?.let { binding.inputPasswordConfirm.setText(it) }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            viewModel.setRegistrationFields(
                RegistrationFields(
                    inputEmail.text.toString(),
                    inputUsername.text.toString(),
                    inputPassword.text.toString(),
                    inputPasswordConfirm.text.toString()
                )
            )
        }

    }


}
