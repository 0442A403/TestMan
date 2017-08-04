package com.android.petro.testman.Support

import java.util.*
import kotlin.collections.ArrayList

/**
 * Support class for transferring tasks information throw callback
 */
class TasksData(val tasks: ArrayList<TaskClass>) {
    constructor(): this(ArrayList(Arrays.asList(TaskClass())))

    fun isFilled(): Boolean {
        for (task in tasks)
            if (task.isEmpty())
                return false
        return true
    }
}

