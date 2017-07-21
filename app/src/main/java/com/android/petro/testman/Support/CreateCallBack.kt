package com.android.petro.testman.Support

import android.support.annotation.MainThread

/**
 * Interface that allows transfer signal and information
 * from SettingsFragment to CreateFragment through
 * SettingsData
 */
interface CreateCallBack {
    fun onTestSave(settings : SettingsData)
    fun checkEmpty(): Boolean
    fun onSaved()
}