package com.android.petro.testman.Support.Listeners

import com.android.petro.testman.Support.TestData.Answer

/**
 * Created by petro on 17.08.2017.
 * Listener for receiving answer.
 */
interface OnAnswerReceivedListener {
    fun onAnswerReceived(answer: Answer)
}