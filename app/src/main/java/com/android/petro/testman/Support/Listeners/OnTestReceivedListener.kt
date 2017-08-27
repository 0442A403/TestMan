package com.android.petro.testman.Support.Listeners

import com.android.petro.testman.Support.TestData.Answer
import com.android.petro.testman.Support.TestData.Test

/**
 * Created by petro on 27.07.2017.
 * Callback for test's receiving
 */
interface OnTestReceivedListener {
    fun onTestReceived(test: Test, receivedId: Int)
    fun onTestReceived(test: Test, answer: Answer?)
    fun onTestReceived(test: Test)
}