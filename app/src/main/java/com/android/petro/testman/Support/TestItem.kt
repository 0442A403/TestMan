package com.android.petro.testman.Support

import java.util.*

/**
 * Created by petro on 23.08.2017.
 * Class for holding test item's information
 */
class TestItem(
        val testId: Int,
        val testName: String,
        val date: Long,
        val answers: ArrayList<AnswerItem>)