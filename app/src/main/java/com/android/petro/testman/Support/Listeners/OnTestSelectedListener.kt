package com.android.petro.testman.Support.Listeners

import android.support.v4.app.Fragment


/**
 * Created by petro on 23.08.2017.
 * Callback for selecting tests in MyTestsControlFragment
 */
interface OnTestSelectedListener {
    fun onTestSelected(fragment: Fragment)
}