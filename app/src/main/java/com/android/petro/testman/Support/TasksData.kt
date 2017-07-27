package com.android.petro.testman.Support

/**
 * Support class for transferring tasks information throw callback
 */
class TasksData() {
    var tasks : ArrayList<TaskClass>? = null
    constructor(mTasks: ArrayList<TaskClass>) : this() {
        tasks = mTasks
    }

}


