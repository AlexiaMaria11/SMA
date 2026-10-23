package com.example.lab4

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Nivelul de prioritate al unui task. `label` e textul afișat în UI. */
enum class Priority(val label: String) {
    LOW("Scăzută"),
    MEDIUM("Medie"),
    HIGH("Ridicată")
}

/**
 * Modelul unui task. Câmpurile sunt `val` (imutabile): când modificăm un task,
 * creăm o copie cu `copy(...)`, ca Compose să observe schimbarea.
 *
 * `dueDate` e în milisecunde, la miezul nopții UTC - exact formatul pe care îl
 * returnează DatePicker-ul din Material3. `null` = fără dată limită.
 */
data class Task(
    val id: Int,
    val title: String,
    val description: String = "",
    val isDone: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Long? = null
)

private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/** Ex.: "25 oct. 2026". Folosim UTC, fiindcă DatePicker lucrează în UTC. */
fun formatDueDate(millis: Long): String {
    val format = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("ro"))
    format.timeZone = UTC
    return format.format(Date(millis))
}

/** Ziua de azi (după ora locală), exprimată ca miezul nopții UTC, ca să o putem compara cu `dueDate`. */
private fun todayUtcMillis(): Long {
    val local = Calendar.getInstance()
    val utc = Calendar.getInstance(UTC)
    utc.clear()
    utc.set(
        local.get(Calendar.YEAR),
        local.get(Calendar.MONTH),
        local.get(Calendar.DAY_OF_MONTH)
    )
    return utc.timeInMillis
}

/** Un task e întârziat dacă nu e terminat și termenul lui a trecut. */
fun Task.isOverdue(): Boolean {
    val due = dueDate ?: return false
    return !isDone && due < todayUtcMillis()
}
