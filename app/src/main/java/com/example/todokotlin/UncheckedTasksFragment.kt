package com.example.todokotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todokotlin.TaskAdapter
import com.example.todokotlin.Task

class UncheckedTasksFragment : Fragment() {
    var tasks: List<Task> = emptyList()
    var onEdit: ((Task) -> Unit)? = null
    var onDelete: ((Task) -> Unit)? = null
    var selectedDate: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tasks, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val filtered = tasks.filter { !it.done && isSameDay(it.dueDate, selectedDate) }
        recyclerView.adapter = TaskAdapter(
            filtered.toMutableList(),
            onEdit ?: {},
            onDelete ?: {},
            onStatusChanged = {
                tasks = tasks.filter { !it.done }
                (activity as? MainActivity)?.showCurrentTabFragment()
            }
        )
        return view
    }

    private fun isSameDay(date1: Long?, date2: Long): Boolean {
        if (date1 == null) return false
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = date2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
