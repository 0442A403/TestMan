package com.android.petro.testman.Support.Listeners

import com.android.petro.testman.Support.Other.TestItem

/**
 * Created by petro on 23.08.2017.
 * Callback for intercommunicate with data
 */
interface OnDataReceivedListener {
    fun onDataReceived(data: ArrayList<TestItem>)
}