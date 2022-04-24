package com.sonu.openapi.di.auth

import androidx.lifecycle.ViewModel
import com.sonu.openapi.di.ViewModelKey
import com.sonu.openapi.ui.auth.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}