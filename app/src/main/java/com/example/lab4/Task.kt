package com.example.lab4

/**
 * Modelul unui task.
 * Toate câmpurile sunt `val` (imutabile): când modificăm un task, creăm o copie
 * cu `copy(...)`. Așa Compose observă schimbarea și redesenează lista.
 */
data class Task(
    val id: Int,
    val title: String,
    val description: String = "",
    val isDone: Boolean = false
)
