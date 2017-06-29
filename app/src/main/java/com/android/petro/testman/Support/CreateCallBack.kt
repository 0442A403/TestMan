package com.android.petro.testman.Support

/**
 * Interface that allows transfer signal and information
 * from SettingsFragment to CreateFragment through
 * SettingsData
 */
interface CreateCallBack {
    fun onTestSave(settings : SettingsData)
}