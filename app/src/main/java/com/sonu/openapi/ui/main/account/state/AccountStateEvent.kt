package com.sonu.openapi.ui.main.account.state

sealed class AccountStateEvent{

    object GetAccountPropertiesEvent : AccountStateEvent()

    data class UpdateAccountPropertiesEvent(
        val email: String,
        val username: String
    ): AccountStateEvent()

    data class ChangePasswordEvent(
        val currentPassword: String,
        val newPassword: String,
        val confirmNewPassword: String
    ) : AccountStateEvent()

    class None: AccountStateEvent()
}