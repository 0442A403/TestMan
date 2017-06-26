package com.android.petro.testman.Support

/**
 * Interface that allows transfer signal and information
 * from SettingsFragment to CreateFragment through
 * SettingsClass
 */
interface CreateCallBack {
    fun onTestSave(settings : SettingsClass)
}