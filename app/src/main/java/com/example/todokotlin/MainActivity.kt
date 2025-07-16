package com.example.todokotlin

import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.todokotlin.R
import com.example.todokotlin.Task
import com.example.todokotlin.TaskAdapter
import kotlin.collections.remove

class MainActivity : AppCompatActivity() {
    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = TaskAdapter(tasks, ::editTask, ::deleteTask)
        findViewById<RecyclerView>(R.id.recyclerViewTasks).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        findViewById<FloatingActionButton>(R.id.fabAddTask).setOnClickListener {
            showTaskDialog(null)
        }
    }

    private fun showTaskDialog(task: Task?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descInput = dialogView.findViewById<EditText>(R.id.editTextDescription)

        if (task != null) {
            titleInput.setText(task.title)
            descInput.setText(task.description)
        }

        AlertDialog.Builder(this)
            .setTitle(if (task == null) "Add Task" else "Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString()
                val desc = descInput.text.toString()
                if (task == null) {
                    val newTask = Task(tasks.size + 1, title, desc)
                    tasks.add(newTask)
                } else {
                    task.title = title
                    task.description = desc
                }
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editTask(task: Task) {
        showTaskDialog(task)
    }

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete?")
            .setPositiveButton("Yes") { _, _ ->
                tasks.remove(task)
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("No", null)
            .show()
    }
}