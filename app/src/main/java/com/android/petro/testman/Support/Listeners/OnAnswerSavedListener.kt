package com.android.petro.testman.Support.Listeners

import com.android.petro.testman.Support.TestData.Answer

/**
 * Created by petro on 27.07.2017.
 * Callback for answer saving
 */

interface OnAnswerSavedListener {
    fun onAnswerSaved(answer: Answer, mark: Int)
}