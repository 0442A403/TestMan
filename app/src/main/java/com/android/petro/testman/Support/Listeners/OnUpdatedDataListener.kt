package com.android.petro.testman.Support.Listeners

import org.json.JSONArray

/**
 * Created by petro on 11.08.2017.
 * Callback for notifying that searched test received
 */
interface OnUpdatedDataListener {
    fun onUpdatedData(array: JSONArray?)
}
