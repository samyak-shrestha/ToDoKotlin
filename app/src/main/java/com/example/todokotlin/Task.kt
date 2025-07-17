package com.example.todokotlin

data class Task(
    var id: Int,
    var title: String,
    var description: String,
    var done: Boolean = false,
    var checkedTime: Long? = null,
    var dueDate: Long? = null,
    var remindMe: Boolean = true
)