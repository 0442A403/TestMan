package com.android.petro.testman.Support.Listeners

import com.android.petro.testman.Support.TestData.SettingsData

/**
 * Interface that allows transfer signal and information
 * from SettingsFragment to CreateFragment through
 * SettingsData
 */
interface OnTestSaveListener: OnTestSavedListener, HasEmpty {
    fun onTestSaving(settings : SettingsData)
}