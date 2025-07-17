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
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import com.example.todokotlin.UncheckedTasksFragment
import com.example.todokotlin.CheckedTasksFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val tasks = mutableListOf<Task>()
    private var selectedDate: Long = System.currentTimeMillis()
    private lateinit var tabLayout: TabLayout
    private lateinit var dateTextView: TextView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)
        dateTextView = findViewById(R.id.textViewSelectedDate)
        fab = findViewById(R.id.fabAddTask)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateTextView.text = dateFormat.format(selectedDate)
        dateTextView.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDate = cal.timeInMillis
                dateTextView.text = dateFormat.format(selectedDate)
                showCurrentTabFragment()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        tabLayout.addTab(tabLayout.newTab().setText("To Do"))
        tabLayout.addTab(tabLayout.newTab().setText("Done"))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                showCurrentTabFragment()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        fab.setOnClickListener {
            showTaskDialog(null)
        }

        showCurrentTabFragment()
    }

    fun showCurrentTabFragment() {
        val fragment = if (tabLayout.selectedTabPosition == 0) {
            UncheckedTasksFragment().apply {
                tasks = this@MainActivity.tasks
                selectedDate = this@MainActivity.selectedDate
                onEdit = { editTask(it) }
                onDelete = { deleteTask(it) }
            }
        } else {
            CheckedTasksFragment().apply {
                tasks = this@MainActivity.tasks
                selectedDate = this@MainActivity.selectedDate
                onEdit = { editTask(it) }
                onDelete = { deleteTask(it) }
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showTaskDialog(task: Task?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descInput = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val dueDateInput = dialogView.findViewById<EditText>(R.id.editTextDueDate)
        val dueTimeInput = dialogView.findViewById<EditText>(R.id.editTextDueTime)
        val remindMeCheckBox = dialogView.findViewById<CheckBox>(R.id.checkBoxRemindMe)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        if (task?.dueDate != null) {
            calendar.timeInMillis = task.dueDate!!
            dueDateInput.setText(dateFormat.format(calendar.time))
        }
        if (task?.dueTime != null) {
            calendar.timeInMillis = task.dueTime!!
            dueTimeInput.setText(timeFormat.format(calendar.time))
        }

        dueDateInput.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                dueDateInput.setText(dateFormat.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        dueTimeInput.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                dueTimeInput.setText(timeFormat.format(calendar.time))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
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
            val dueTime = calendar.timeInMillis
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
                val newTask = Task(tasks.size + 1, title, desc, dueDate = dueDate, dueTime = dueTime, remindMe = remindMe)
                tasks.add(newTask)
            } else {
                task.title = title
                task.description = desc
                task.dueDate = dueDate
                task.dueTime = dueTime
                task.remindMe = remindMe
            }
            showCurrentTabFragment()
            alertDialog.dismiss()
        }
    }

    private fun editTask(task: Task) {
        showTaskDialog(task)
        showCurrentTabFragment()
    }

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete?")
            .setPositiveButton("Yes") { _, _ ->
                tasks.remove(task)
                showCurrentTabFragment()
            }
            .setNegativeButton("No", null)
            .show()
    }
}