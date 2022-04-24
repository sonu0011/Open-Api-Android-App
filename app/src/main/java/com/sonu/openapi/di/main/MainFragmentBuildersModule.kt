package com.sonu.openapi.di.main

import com.sonu.openapi.ui.main.account.AccountFragment
import com.sonu.openapi.ui.main.account.ChangePasswordFragment
import com.sonu.openapi.ui.main.account.UpdateAccountFragment
import com.sonu.openapi.ui.main.blog.BlogFragment
import com.sonu.openapi.ui.main.blog.UpdateBlogFragment
import com.sonu.openapi.ui.main.blog.ViewBlogFragment
import com.sonu.openapi.ui.main.create_blog.CreateBlogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeBlogFragment(): BlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector()
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector()
    abstract fun contributeCreateBlogFragment(): CreateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateBlogFragment(): UpdateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeViewBlogFragment(): ViewBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateAccountFragment(): UpdateAccountFragment
}