package com.example.plango.model

import java.time.LocalDate

data class Calendar(
    val day: Int,
    val date: LocalDate,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val hasSchedule: Boolean = false
)

