package com.example.praktikumroom
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.praktikumroom.ui.theme.PraktikumRoomTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
class MainActivity : ComponentActivity() {
    // Inisialisasi Database dan ViewModel
    private val database by lazy { TaskDatabase.getDatabase(application) }
    private val repository by lazy { TaskRepository(database.taskDao()) }
    private val viewModelFactory by lazy { TaskViewModelFactory(repository) }
    private val viewModel: TaskViewModel by viewModels { viewModelFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PraktikumRoomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
// Ambil data Flow dan konversi menjadi Compose State
                    val tasks = viewModel.allTasks.collectAsState(initial =
                        emptyList()).value
                    TaskScreen(
                        tasks = tasks,
                        onAddTask = { title -> viewModel.addNewTask(title) },
                        onUpdateTask = { task, completed -> viewModel.updateTaskStatus(task, completed) },
                        onUpdateTaskTitle = { task, newTitle -> viewModel.updateTaskTitle(task, newTitle) }, // Parameter baru
                        onDeleteTask = { task -> viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    tasks: List<Task>,
    onAddTask: (String) -> Unit,
    onUpdateTask: (Task, Boolean) -> Unit,
    onUpdateTaskTitle: (Task, String) -> Unit, // Parameter baru
    onDeleteTask: (Task) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Daftar Tugas (Room Compose)") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TaskInput(onAddTask)
            Spacer(modifier = Modifier.height(8.dp))

            TaskList(tasks, onUpdateTask, onUpdateTaskTitle, onDeleteTask) // Update pemanggilan
        }
    }
}
@Composable
fun TaskInput(onAddTask: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Tugas Baru") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (text.isNotBlank()) {
                    onAddTask(text)
                    text = "";
                }
            },
            enabled = text.isNotBlank()
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Tambah Tugas")
        }
    }
}
@Composable
fun TaskList(
    tasks: List<Task>,
    onUpdateTask: (Task, Boolean) -> Unit,
    onUpdateTaskTitle: (Task, String) -> Unit, // Parameter baru
    onDeleteTask: (Task) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        items(tasks, key = { it.id }) { task ->
            TaskItem(
                task = task,
                onCheckedChange = { isChecked -> onUpdateTask(task, isChecked) },
                onUpdateTitle = { newTitle -> onUpdateTaskTitle(task, newTitle) }, // Parameter baru
                onDelete = { onDeleteTask(task) }
            )
            Divider()
        }
    }
}
@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onUpdateTitle: (String) -> Unit, // Parameter baru
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(task.title) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange,
            )
            Spacer(Modifier.width(8.dp))

            if (isEditing) {
                // Mode edit
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Masukkan judul tugas") }
                )
            } else {
                // Mode tampilan
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!task.isCompleted) {
                                isEditing = true
                            }
                        }
                )
            }

            Spacer(Modifier.width(8.dp))

            // Tombol aksi
            if (isEditing) {
                // Kosongkan tombol aksi saat mode edit
            } else {
                IconButton(
                    onClick = onDelete,
                    enabled = !isEditing
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Hapus Tugas")
                }
                IconButton(
                    onClick = {
                        if (!task.isCompleted) {
                            isEditing = true
                        }
                    },
                    enabled = !task.isCompleted && !isEditing
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Tugas")
                }
            }
        }

        // Tombol Simpan dan Batal
        if (isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        isEditing = false
                        editedTitle = task.title
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        if (editedTitle.isNotBlank()) {
                            onUpdateTitle(editedTitle)
                            isEditing = false
                        }
                    },
                    enabled = editedTitle.isNotBlank()
                ) {
                    Text("Simpan")
                }
            }
        }
    }
}