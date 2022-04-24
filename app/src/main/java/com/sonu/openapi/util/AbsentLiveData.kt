package com.sonu.openapi.util

import androidx.lifecycle.LiveData

/*
    Absent live data that has a nul  value
 */
class AbsentLiveData<T : Any?> private constructor() : LiveData<T>() {
    init {
        postValue(null)
    }

    companion object {
        fun <T> create(): LiveData<T> {
            return AbsentLiveData()
        }
    }
}