package com.twotickets.tasksngoals.core.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun LocalDate.toViewFormat() = format(DateTimeFormatter.ofPattern("EEE, d LLL yyyy"))

fun LocalTime.toViewFormat() = format(DateTimeFormatter.ofPattern("HH:mm"))

fun LocalDateTime.toViewFormat() = format(DateTimeFormatter.ofPattern("EEE, d LLL yyyy 'at' HH:mm"))

fun getDateTimeViewFormat(date: LocalDate, time: LocalTime?): String {
    return time?.let { date.atTime(it.hour, it.minute) }?.toViewFormat() ?: date.toViewFormat()
}