package com.sonu.openapi.di

import com.sonu.openapi.di.auth.AuthFragmentBuildersModule
import com.sonu.openapi.di.auth.AuthModule
import com.sonu.openapi.di.auth.AuthScope
import com.sonu.openapi.di.auth.AuthViewModelModule
import com.sonu.openapi.di.main.MainFragmentBuildersModule
import com.sonu.openapi.di.main.MainModule
import com.sonu.openapi.di.main.MainScope
import com.sonu.openapi.di.main.MainViewModelModule
import com.sonu.openapi.ui.auth.AuthActivity
import com.sonu.openapi.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainModule::class, MainFragmentBuildersModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity

}