package com.android.petro.testman.Support.Listeners

import com.android.petro.testman.Support.TestData.SettingsData

/**
 * Created by petro on 26.08.2017.
 * Callback for updating tests
 */
interface OnTestUpdateListener: OnTestUpdatedListener, HasEmpty {
    fun onTestUpdated(settings: SettingsData)
    fun checkTasksHasBeenChanged(): Boolean
}