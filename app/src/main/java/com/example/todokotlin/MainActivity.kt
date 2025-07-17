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
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        val dueDateInput = dialogView.findViewById<EditText>(R.id.editTextDueDate)
        val remindMeCheckBox = dialogView.findViewById<CheckBox>(R.id.checkBoxRemindMe)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        if (task?.dueDate != null) {
            calendar.timeInMillis = task.dueDate!!
        }
        dueDateInput.setText(dateFormat.format(calendar.time))

        dueDateInput.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                TimePickerDialog(this, { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    dueDateInput.setText(dateFormat.format(calendar.time))
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        if (task != null) {
            titleInput.setText(task.title)
            descInput.setText(task.description)
            remindMeCheckBox.isChecked = task.remindMe
        }

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(if (task == null) "Add Task" else "Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val desc = descInput.text.toString().trim()
            val dueDate = calendar.timeInMillis
            val remindMe = remindMeCheckBox.isChecked
            if (title.isEmpty()) {
                titleInput.error = "Title cannot be blank"
                titleInput.requestFocus()
                Toast.makeText(this, "Title cannot be blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (desc.isEmpty()) {
                descInput.error = "Description cannot be blank"
                descInput.requestFocus()
                Toast.makeText(this, "Description cannot be blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (task == null) {
                val newTask = Task(tasks.size + 1, title, desc, dueDate = dueDate, remindMe = remindMe)
                tasks.add(newTask)
            } else {
                task.title = title
                task.description = desc
                task.dueDate = dueDate
                task.remindMe = remindMe
            }
            adapter.sortTasks()
            alertDialog.dismiss()
        }
    }

    private fun editTask(task: Task) {
        showTaskDialog(task)
        adapter.sortTasks()
    }

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete?")
            .setPositiveButton("Yes") { _, _ ->
                tasks.remove(task)
                adapter.sortTasks()
            }
            .setNegativeButton("No", null)
            .show()
    }
}