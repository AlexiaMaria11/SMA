package com.example.lab4

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

/**
 * Ține lista de task-uri și toată logica (add / edit / toggle / delete / mutare).
 * Ordinea din listă = ordinea afișată, deci drag and drop-ul doar mută elemente aici.
 * ViewModel-ul supraviețuiește rotirii ecranului; la închiderea aplicației lista se pierde.
 */
class TaskViewModel : ViewModel() {

    private val _tasks = mutableStateListOf<Task>()

    // Expunem doar o vedere read-only, ca UI-ul să nu poată modifica lista direct.
    val tasks: List<Task> get() = _tasks

    private var nextId = 1

    fun addTask(title: String, description: String, priority: Priority, dueDate: Long?) {
        if (title.isBlank()) return
        _tasks.add(
            Task(
                id = nextId++,
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                dueDate = dueDate
            )
        )
    }

    fun updateTask(
        id: Int,
        title: String,
        description: String,
        priority: Priority,
        dueDate: Long?
    ) {
        if (title.isBlank()) return
        val index = _tasks.indexOfFirst { it.id == id }
        if (index == -1) return
        _tasks[index] = _tasks[index].copy(
            title = title.trim(),
            description = description.trim(),
            priority = priority,
            dueDate = dueDate
        )
    }

    fun toggleDone(id: Int) {
        val index = _tasks.indexOfFirst { it.id == id }
        if (index == -1) return
        _tasks[index] = _tasks[index].copy(isDone = !_tasks[index].isDone)
    }

    fun deleteTask(id: Int) {
        _tasks.removeAll { it.id == id }
    }

    /** Mută task-ul de pe poziția `fromIndex` pe `toIndex` (folosit de drag and drop). */
    fun moveTask(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex !in _tasks.indices || toIndex !in _tasks.indices) return
        _tasks.add(toIndex, _tasks.removeAt(fromIndex))
    }
}
