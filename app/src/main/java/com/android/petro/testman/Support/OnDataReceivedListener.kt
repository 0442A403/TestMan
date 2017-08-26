package com.android.petro.testman.Support

/**
 * Created by petro on 23.08.2017.
 * Callback for intercommunicate with data
 */
interface OnDataReceivedListener {
    fun onDataReceived(data: ArrayList<TestItem>)
}