package com.sonu.openapi.ui

interface UICommunicationListener {
    fun onUIMessageReceived(uiMessage: UIMessage)
}