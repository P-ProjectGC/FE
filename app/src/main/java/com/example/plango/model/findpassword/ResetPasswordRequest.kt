package com.example.plango.model.findpassword

data class ResetPasswordRequest(
    val loginId: String,
    val newPassword: String,
    val newPasswordConfirm: String
)