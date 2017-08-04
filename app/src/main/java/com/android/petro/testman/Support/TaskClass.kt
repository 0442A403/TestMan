package com.android.petro.testman.Support

import java.util.*
import kotlin.collections.ArrayList


/**
 * Class storing information about task
 */

class TaskClass(val question: String = "",
                val answers: ArrayList<String> = ArrayList(Arrays.asList("", "")),
                val type: Int = TaskType.RADIO_BOX.code,
                val rights: Any = -1,
                val photoArray: Any = Object()) {
    fun isEmpty(): Boolean {
        return question.isEmpty()
                || answers.contains("")
                || if (type == TaskType.RADIO_BOX.code) (rights as Int) < 0 else (rights as ArrayList<*>).size > 0
    }
}
