package com.android.petro.testman.Support

/**
 * Created by petro on 27.07.2017.
 * Callback for test's receiving
 */
interface OnTestReceive {
    fun onTestReceived(test: TestClass, receivedId: Int)
}