package com.android.petro.testman.Support

/**
 * Created by petro on 27.07.2017.
 * Callback for test's receiving
 */
interface OnTestReceivedListener {
    fun onTestReceived(test: Test, receivedId: Int)
    fun onTestReceived(test: Test, answer: Answer?)
    fun onTestReceived(test: Test)
}