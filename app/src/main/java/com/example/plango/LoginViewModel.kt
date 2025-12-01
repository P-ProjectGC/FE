package com.example.plango

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    val id = MutableLiveData("")
    val pw = MutableLiveData("")
    val loginResult = MutableLiveData<Boolean>()   // 성공/실패

    fun canLogin(): Boolean {
        return !id.value.isNullOrEmpty() && !pw.value.isNullOrEmpty()
    }

    fun login() {
        // 테스트용: 무조건 id= "test", pw = "1234"일 때만 성공
        if (id.value == "test" && pw.value == "1234") {
            loginResult.value = true
        } else {
            loginResult.value = false
        }
    }
}
