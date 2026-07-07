package com.habizy.app.data.model

// -- Requests --

data class MenageSubTaskRequest(
    val text: String
)

data class MarkMenageDoneRequest(
    val comment: String? = null
)

data class UpdateTaskDescriptionRequest(
    val description: String
)

data class UpdateSubTaskLimitRequest(
    val limit: Int
)

// -- Responses --

data class MenageSubTask(
    val text: String,
    val completedAt: String
)

data class MenageBoardMember(
    val userId: String,
    val name: String,
    val initial: String? = null,
    val colorHex: String? = null,
    val done: Boolean,
    val doneAt: String? = null,
    val comment: String? = null,
    val subTasks: List<MenageSubTask>? = null
)

data class MenageWeekResponse(
    val weekStart: String,
    val board: List<MenageBoardMember>,
    val totalMembers: Int,
    val totalDone: Int,
    val todayTakenBy: String? = null,
    val taskDescription: String? = null,
    val subTaskLimit: Int = 10
)
