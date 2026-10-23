@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.lab4

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Ecranul principal: titlu, lista de task-uri (cu drag and drop) și butonul "+".
 */
@Composable
fun TaskScreen(viewModel: TaskViewModel = viewModel()) {
    // Stare de UI: dialogul de adăugare/editare
    var showDialog by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }

    // Stare pentru drag and drop
    val listState = rememberLazyListState()
    var draggedId by remember { mutableStateOf<Int?>(null) }   // task-ul pe care îl tragem acum
    var dragInitialOffset by remember { mutableFloatStateOf(0f) } // unde era el când am început tragerea
    var dragDistance by remember { mutableFloatStateOf(0f) }      // cât am tras de atunci

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    taskBeingEdited = null      // null = task nou
                    showDialog = true
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adaugă task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Header(
                total = viewModel.tasks.size,
                done = viewModel.tasks.count { it.isDone }
            )

            if (viewModel.tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Niciun task încă.\nApasă + ca să adaugi unul.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.tasks, key = { it.id }) { task ->
                        val isDragged = draggedId == task.id

                        // Task-ul tras "plutește" sub deget; celelalte se mută animat.
                        val itemModifier = if (isDragged) {
                            Modifier
                                .zIndex(1f)
                                .graphicsLayer {
                                    val currentOffset = listState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { it.key == task.id }?.offset ?: 0
                                    translationY = dragInitialOffset + dragDistance - currentOffset
                                }
                        } else {
                            Modifier.animateItem()
                        }

                        // Gestul de tragere stă doar pe iconița de "mâner" din stânga.
                        val dragHandleModifier = Modifier.pointerInput(task.id) {
                            detectDragGestures(
                                onDragStart = {
                                    val info = listState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { it.key == task.id }
                                    if (info != null) {
                                        draggedId = task.id
                                        dragInitialOffset = info.offset.toFloat()
                                        dragDistance = 0f
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (draggedId != task.id) return@detectDragGestures
                                    dragDistance += dragAmount.y

                                    val visible = listState.layoutInfo.visibleItemsInfo
                                    val dragged = visible.firstOrNull { it.key == task.id }
                                        ?: return@detectDragGestures

                                    // Mijlocul task-ului tras; dacă a ajuns peste alt task, schimbăm locurile.
                                    val center = dragInitialOffset + dragDistance + dragged.size / 2f
                                    val target = visible.firstOrNull {
                                        it.key != task.id &&
                                            center >= it.offset &&
                                            center <= it.offset + it.size
                                    } ?: return@detectDragGestures

                                    val from = viewModel.tasks.indexOfFirst { it.id == task.id }
                                    val to = viewModel.tasks.indexOfFirst { it.id == target.key }
                                    // Dacă informațiile de layout nu s-au actualizat încă după ultima
                                    // mutare, sărim peste acest eveniment (evită schimbări înapoi-înainte).
                                    if (from != dragged.index || to != target.index) return@detectDragGestures
                                    viewModel.moveTask(from, to)
                                },
                                onDragEnd = {
                                    draggedId = null
                                    dragDistance = 0f
                                },
                                onDragCancel = {
                                    draggedId = null
                                    dragDistance = 0f
                                }
                            )
                        }

                        TaskItem(
                            task = task,
                            isDragged = isDragged,
                            modifier = itemModifier,
                            dragHandleModifier = dragHandleModifier,
                            onToggle = { viewModel.toggleDone(task.id) },
                            onClick = {
                                taskBeingEdited = task
                                showDialog = true
                            },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        TaskDialog(
            initialTask = taskBeingEdited,
            onDismiss = { showDialog = false },
            onConfirm = { title, description, priority, dueDate ->
                val editing = taskBeingEdited
                if (editing == null) {
                    viewModel.addTask(title, description, priority, dueDate)
                } else {
                    viewModel.updateTask(editing.id, title, description, priority, dueDate)
                }
                showDialog = false
            }
        )
    }
}

/** Titlul mare și un rând discret cu progresul. */
@Composable
private fun Header(total: Int, done: Int) {
    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)) {
        Text(
            text = "Task-uri",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (total == 0) "Nimic de făcut" else "$done din $total finalizate",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.LOW -> Color(0xFF8E9AAF)
    Priority.MEDIUM -> Color(0xFFE0A100)
    Priority.HIGH -> Color(0xFFD64545)
}

/**
 * Un rând din listă: mâner de tragere, bifă, titlu / descriere / prioritate / dată, buton de ștergere.
 * Apăsarea pe rând deschide editarea. Nu știe nimic despre ViewModel (state hoisting).
 */
@Composable
fun TaskItem(
    task: Task,
    isDragged: Boolean,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val shadow by animateDpAsState(if (isDragged) 10.dp else 0.dp, label = "dragShadow")

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = shadow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Ține apăsat și trage pentru a muta",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = dragHandleModifier
                    .padding(horizontal = 12.dp, vertical = 14.dp)
                    .size(20.dp)
            )
            Checkbox(checked = task.isDone, onCheckedChange = { onToggle() })

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
                    .alpha(if (task.isDone) 0.5f else 1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(priorityColor(task.priority))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = task.priority.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    task.dueDate?.let { due ->
                        val dateColor = if (task.isOverdue()) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = dateColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = formatDueDate(due),
                            style = MaterialTheme.typography.labelSmall,
                            color = dateColor
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Șterge",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Același dialog pentru "task nou" și "editează task".
 * Dacă initialTask e null -> câmpuri goale; altfel -> precompletate.
 */
@Composable
fun TaskDialog(
    initialTask: Task?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, priority: Priority, dueDate: Long?) -> Unit
) {
    var title by remember(initialTask) { mutableStateOf(initialTask?.title ?: "") }
    var description by remember(initialTask) { mutableStateOf(initialTask?.description ?: "") }
    var priority by remember(initialTask) { mutableStateOf(initialTask?.priority ?: Priority.MEDIUM) }
    var dueDate by remember(initialTask) { mutableStateOf(initialTask?.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTask == null) "Task nou" else "Editează task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titlu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descriere (opțional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Prioritate",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Priority.entries.forEach { option ->
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(option.label, maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = "Termen",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(dueDate?.let { formatDueDate(it) } ?: "Alege data")
                    }
                    if (dueDate != null) {
                        IconButton(onClick = { dueDate = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Șterge data")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, description, priority, dueDate) },
                enabled = title.isNotBlank()   // nu poți salva un task fără titlu
            ) {
                Text("Salvează")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anulează") }
        }
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = pickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anulează") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
