package com.example.plango.model


import java.time.LocalDate

data class CalendarDay_rm(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)
