# Praktikum Room

Aplikasi **Daftar Tugas (To-Do List)** yang menggunakan **Room Database** sebagai penyimpanan lokal dan **Jetpack Compose** untuk tampilan UI. Aplikasi ini mendukung penambahan, pengeditan, penghapusan, serta penandaan status tugas.

---

## Fitur Aplikasi

| Fitur | Deskripsi |
|------|-----------|
| Tambah tugas | Pengguna dapat menambahkan tugas baru melalui input field |
| Tandai selesai | Pengguna dapat menandai tugas selesai/aktif menggunakan checkbox |
| Edit judul tugas | Klik teks tugas untuk masuk mode edit |
| Hapus tugas | Tugas dapat dihapus melalui tombol delete |
| Penyimpanan Lokal | Data tersimpan menggunakan **Room**, tidak hilang meski aplikasi ditutup |

---

## Arsitektur

Aplikasi menggunakan pola **MVVM (Model - ViewModel - View)**:

| Fitur | File / Komponen | Fungsi |
|-----------|-----------|-----------|
| Model	| Task.kt	| Mendefinisikan struktur entitas tabel Room |
| DAO |	TaskDao.kt | Menentukan query untuk insert, read, update, delete |
| Database |	TaskDatabase.kt	| Singleton Room Database untuk akses DAO |
| Repository	| TaskRepository.kt	| Perantara antara DAO dan ViewModel |
| ViewModel | TaskViewModel.kt | Menyediakan data reaktif (Flow) ke UI dan menjalankan operasi coroutine |
| UI (View)	| MainActivity.kt, komponen Compose (TaskScreen, TaskList) |	Menampilkan data & berinteraksi dengan pengguna |

---

## Entity (Model)

Entity utama aplikasi adalah Task

```kotlin
@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false
)
```
---

## DAO (Database Access Object)

```kotlin
@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM task_table ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE task_table SET title = :title WHERE id = :id")
    suspend fun updateTaskTitle(id: Int, title: String)
}
```

---

## Room Database

```kotlin
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

```

---

## Repository

```kotlin
class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun insert(task: Task) = taskDao.insertTask(task)
    suspend fun update(task: Task) = taskDao.updateTask(task)
    suspend fun delete(task: Task) = taskDao.deleteTask(task)
    suspend fun updateTitle(id: Int, title: String) = taskDao.updateTaskTitle(id, title)
}
```

---

ViewModel

```kotlin
class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val allTasks = repository.allTasks

    fun addNewTask(taskTitle: String) {
        viewModelScope.launch { repository.insert(Task(title = taskTitle)) }
    }

    fun updateTaskStatus(task: Task, isCompleted: Boolean) {
        viewModelScope.launch { repository.update(task.copy(isCompleted = isCompleted)) }
    }

    fun updateTaskTitle(task: Task, newTitle: String) {
        viewModelScope.launch { repository.updateTitle(task.id, newTitle) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }
}
```

---

## UI (Jetpack Compose)

```kotlin
val tasks = viewModel.allTasks.collectAsState(initial = emptyList()).value
TaskScreen(
    tasks = tasks,
    onAddTask = { title -> viewModel.addNewTask(title) },
    onUpdateTask = { task, completed -> viewModel.updateTaskStatus(task, completed) },
    onUpdateTaskTitle = { task, newTitle -> viewModel.updateTaskTitle(task, newTitle) },
    onDeleteTask = { task -> viewModel.deleteTask(task) }
)
```

---

## Kesimpulan

1. Aplikasi ini menunjukkan penerapan:
2. Room Database untuk penyimpanan lokal
3. Jetpack Compose untuk UI modern berbasis deklaratif
4. MVVM + Repository untuk pemisahan logika yang bersih
5. Flow + Coroutine untuk data real-time yang efisien
