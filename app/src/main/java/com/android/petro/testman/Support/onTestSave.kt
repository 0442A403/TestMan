package com.android.petro.testman.Support

/**
 * Interface that allows transfer signal and information
 * from SettingsFragment to CreateFragment through
 * SettingsData
 */
interface onTestSave {
    fun onTestSaving(settings : SettingsData)
    fun onTestSaved()
    fun hasEmpty(): Boolean
}