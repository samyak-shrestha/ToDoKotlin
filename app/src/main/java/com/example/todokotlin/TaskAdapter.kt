package com.example.todokotlin

import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.textViewTitle)
        val description = itemView.findViewById<TextView>(R.id.textViewDescription)
        val edit = itemView.findViewById<Button>(R.id.buttonEdit)
        val delete = itemView.findViewById<Button>(R.id.buttonDelete)
        val doneCheckBox = itemView.findViewById<CheckBox>(R.id.checkBoxDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        // Always set plain text first
        holder.title.text = task.title
        val preview = task.description.split("\n").firstOrNull()?.take(40) ?: ""
        holder.description.text = preview + if (task.description.length > preview.length) "..." else ""
        // Title click shows details dialog
        holder.title.setOnClickListener {
            val context = holder.itemView.context
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dueDateStr = if (task.dueDate != null) dateFormat.format(Date(task.dueDate!!)) else "No due date set"
            val remindMeStr = if (task.remindMe) "Yes" else "No"
            val details = "Description: ${task.description}\nDue Date: $dueDateStr\nRemind Me: $remindMeStr"
            val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle(task.title)
                .setMessage(details)
                .setPositiveButton("Edit") { _, _ -> onEdit(task) }
                .setNegativeButton("Delete") { _, _ -> onDelete(task) }
                .setNeutralButton("Close", null)
                .create()
            dialog.show()
        }
        holder.edit.setOnClickListener { onEdit(task) }
        holder.delete.setOnClickListener { onDelete(task) }
        holder.doneCheckBox.setOnCheckedChangeListener(null)
        holder.doneCheckBox.isChecked = task.done
        holder.doneCheckBox.setOnCheckedChangeListener { _, isChecked ->
            task.done = isChecked
            if (isChecked) {
                task.checkedTime = System.currentTimeMillis()
            } else {
                task.checkedTime = null
            }
            sortTasks()
        }
        // Apply thick strike-through if checked
        if (task.done) {
            val thickStrikeSpan = object : StrikethroughSpan() {
                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.strokeWidth = 8f // Make the line thick
                }
            }
            val titleStr = SpannableString(task.title)
            titleStr.setSpan(thickStrikeSpan, 0, titleStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.title.setText(titleStr, TextView.BufferType.SPANNABLE)
            val descStr = SpannableString(holder.description.text)
            descStr.setSpan(thickStrikeSpan, 0, descStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.description.setText(descStr, TextView.BufferType.SPANNABLE)
        } else {
            holder.title.setText(task.title, TextView.BufferType.NORMAL)
            holder.description.setText(holder.description.text.toString(), TextView.BufferType.NORMAL)
        }
    }

    override fun getItemCount() = tasks.size

    fun sortTasks() {
        // Group unchecked tasks at the top (by id ascending), checked tasks after (by checkedTime descending)
        val unchecked = tasks.filter { !it.done }.sortedBy { it.id }
        val checked = tasks.filter { it.done }.sortedByDescending { it.checkedTime ?: 0L }
        tasks.clear()
        tasks.addAll(unchecked)
        tasks.addAll(checked)
        notifyDataSetChanged()
    }
}