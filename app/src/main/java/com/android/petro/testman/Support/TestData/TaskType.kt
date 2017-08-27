package com.android.petro.testman.Support.TestData

/**
 * Types of tasks
 */

enum class TaskType(val code: Int) {
    RADIO_BOX(0),
    CHECK_BOX(1),
    LINE(2);
    companion object {
        fun getTypeByCode(code : Int): TaskType = when (code) {
            0 -> RADIO_BOX
            1 -> CHECK_BOX
            else -> LINE
        }
    }
}