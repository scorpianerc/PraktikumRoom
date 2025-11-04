package com.example.praktikumroom
import kotlinx.coroutines.flow.Flow
class TaskRepository(private val taskDao: TaskDao) {
// Fungsi untuk Read: Room sudah menyediakan Flow

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    // Fungsi untuk Create: dipanggil dalam Coroutine
    suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }
    // Fungsi untuk Update: dipanggil dalam Coroutine
    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }
    // Fungsi untuk Update Title: dipanggil dalam Coroutine
    suspend fun updateTitle(id: Int, title: String) {
        taskDao.updateTaskTitle(id, title)
    }
    // Fungsi untuk Delete: dipanggil dalam Coroutine
    suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }
}