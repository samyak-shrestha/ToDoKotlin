package com.example.todokotlin

data class Task(
    var id: Int,
    var title: String,
    var description: String,
    var done: Boolean = false,
    var checkedTime: Long? = null,
    var dueDate: Long? = null, // Stores the date
    var dueTime: Long? = null, // Stores the time
    var remindMe: Boolean = true
)