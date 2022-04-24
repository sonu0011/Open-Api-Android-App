package com.sonu.openapi.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sonu.openapi.databinding.FragmentLoginBinding
import com.sonu.openapi.ui.auth.state.AuthStateEvent
import com.sonu.openapi.ui.auth.state.LoginFields


class LoginFragment : BaseAuthFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
        binding.loginButton.setOnClickListener {
            login()
        }

    }

    private fun login() {
        viewModel.setStateEvent(
            AuthStateEvent.LoginAttemptEvent(
                email = binding.inputEmail.text.toString(),
                password = binding.inputPassword.text.toString()
            )
        )
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) {
            it.loginFields?.let {
                it.login_email?.let { binding.inputEmail.setText(it) }
                it.login_password?.let { binding.inputPassword.setText(it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                binding.inputEmail.text.toString(),
                binding.inputPassword.text.toString()
            )
        )
    }
}
