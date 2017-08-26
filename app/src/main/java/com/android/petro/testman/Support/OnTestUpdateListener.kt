package com.android.petro.testman.Support

/**
 * Created by petro on 26.08.2017.
 */
interface OnTestUpdateListener: OnTestUpdatedListener, HasEmpty {
    fun OnTestUpdate(settings: SettingsData)
}