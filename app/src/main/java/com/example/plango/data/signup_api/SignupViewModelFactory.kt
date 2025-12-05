package com.example.plango.data.signup_api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SignupViewModelFactory(
    private val repository: SignupRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignupViewModel::class.java)) {
            return SignupViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}