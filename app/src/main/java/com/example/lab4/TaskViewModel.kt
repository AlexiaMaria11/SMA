package com.example.lab4

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

/**
 * Ține lista de task-uri și toată logica (add / edit / toggle / delete).
 * ViewModel-ul supraviețuiește rotirii ecranului, deci lista nu se pierde
 * când întorci telefonul (dar se pierde când închizi aplicația - e în memorie).
 */
class TaskViewModel : ViewModel() {

    // Lista observabilă: Compose redesenează UI-ul când se adaugă/șterge/înlocuiește un element.
    private val _tasks = mutableStateListOf<Task>()

    // Expunem doar o vedere read-only, ca UI-ul să nu poată modifica lista direct.
    val tasks: List<Task> get() = _tasks

    private var nextId = 1

    fun addTask(title: String, description: String) {
        if (title.isBlank()) return
        _tasks.add(Task(id = nextId++, title = title.trim(), description = description.trim()))
    }

    fun updateTask(id: Int, title: String, description: String) {
        if (title.isBlank()) return
        val index = _tasks.indexOfFirst { it.id == id }
        if (index == -1) return
        // Înlocuim elementul cu o copie modificată -> Compose detectează schimbarea.
        _tasks[index] = _tasks[index].copy(title = title.trim(), description = description.trim())
    }

    fun toggleDone(id: Int) {
        val index = _tasks.indexOfFirst { it.id == id }
        if (index == -1) return
        _tasks[index] = _tasks[index].copy(isDone = !_tasks[index].isDone)
    }

    fun deleteTask(id: Int) {
        _tasks.removeAll { it.id == id }
    }
}
