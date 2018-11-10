package com.android.petro.testman.Fragments.Create

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import com.android.petro.testman.R
import com.android.petro.testman.Support.Listeners.OnTestSaveListener
import com.android.petro.testman.Support.Listeners.OnTestUpdateListener
import com.android.petro.testman.Support.TestData.SettingsData
import com.pawegio.kandroid.onProgressChanged
import com.pawegio.kandroid.textWatcher
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlin.properties.Delegates

/**
 * Fragment for test's settings
 */

@SuppressLint("ValidFragment")
class SettingsFragment
@SuppressLint("ValidFragment") constructor(private val onTestSaveListener: OnTestSaveListener,
                                           private val onTestUpdateListener: OnTestUpdateListener,
                                           private val isChanging: Boolean = false) : Fragment() {

    private var layout: FrameLayout by Delegates.notNull()
    private val EMPTY_NAME = 1
    private val EMPTY_TIMER = 2
    private var settings: SettingsData? = null

    constructor(onTestSaveListener: OnTestSaveListener,
                onTestUpdateListener: OnTestUpdateListener,
                fillSettings: SettingsData): this(onTestSaveListener, onTestUpdateListener, true) {
        settings = fillSettings
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_settings, container, false) as FrameLayout

        val five = layout.seekbar__five
        val four = layout.seekbar__four
        val three = layout.seekbar__three
        val fiveIndicator = layout.seekbar_indicator__five
        val fourIndicator = layout.seekbar_indicator__four
        val threeIndicator = layout.seekbar_indicator__three
        val preferences = context!!.getSharedPreferences("AppPref", Context.MODE_PRIVATE)

        five.onProgressChanged {
            progress, _ ->
            if (progress < 3)
                five.progress = 3
            if (progress <= four.progress)
                four.progress = progress - 1
            fiveIndicator.text = "${five.progress}%"
        }
        five.progress = preferences.getInt("Five begins", 75)
        four.onProgressChanged {
            progress, _ ->
            if (progress == 100)
                four.progress = 99
            else if (progress < 2)
                four.progress = 2
            if (progress <= three.progress)
                three.progress = progress - 1
            else if (progress >= five.progress)
                five.progress = progress + 1
            fourIndicator.text = "${four.progress}%"
        }
        four.progress = preferences.getInt("Four begins", 50)
        three.onProgressChanged {
            progress, _ ->
            if (progress > 98)
                three.progress = 98
            else if (progress == 0)
                three.progress = 1
            if (progress >= four.progress)
                four.progress = progress + 1
            threeIndicator.text = "${three.progress}%"
        }
        three.progress = preferences.getInt("Three begins", 25)

        val timerCheckBox = layout.timer_checkbox
        val timerWrapper: View = layout.timer_wrapper

        timerCheckBox.setOnCheckedChangeListener {
            _, isChecked : Boolean ->
            if (isChecked)
                timerWrapper.visibility = View.VISIBLE
            else {
                timerWrapper.visibility = View.GONE
                (context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(layout.windowToken, 0)
            }
        }

        val minute = layout.timer_minute
        val second = layout.timer_second

        minute.setOnEditorActionListener { _, _, _ -> second.requestFocus() }
        second.textWatcher {
            onTextChanged { _, _, _, _ ->
                if (second.text?.length == 2 && second.text.toString().toInt() > 59) {
                    second.setText(second.text?.substring(0..0))
                    second.setSelection(1)
                }
            }
        }

        layout.save_test.setOnClickListener { v ->
            when (checkEmpty()) {
                EMPTY_NAME -> Snackbar.make(v, "Введите название", Snackbar.LENGTH_SHORT).show()
                EMPTY_TIMER -> Snackbar.make(v, "Введите время", Snackbar.LENGTH_SHORT).show()
                else -> {
                    if (isChanging && onTestUpdateListener.checkTasksHasBeenChanged())
                        dialogBox("Вы изменили тестовую часть. При сохранении все ответы будут удалены. Сохранить?")
                    else if (!onTestSaveListener.hasEmpty())
                        dialogBox(
                                if (getTime() < 300 && layout.timer_checkbox.isChecked)
                                    "Вы выбрали время, меньшее пяти минут. Сохранить?"
                                else
                                    "Сохранить?"
                        )
                }
            }
        }

        val time = preferences.getInt("Time", 0)
        if (time > 0) {
            timerCheckBox.isChecked = true
            minute.setText((time / 60).toString())
            if (time % 60 > 0)
                second.setText((time % 60).toString())
        }
        layout.show_wrong.isChecked = preferences.getBoolean("Show wrongs", false)

        if (settings != null) {
            layout.test_name__creating.setText(settings!!.name)
            five.progress = settings!!.fivebegins
            four.progress = settings!!.fourbegins
            three.progress = settings!!.threebegins
            layout.show_wrong.isChecked = settings!!.showwrongs == "t"
            if (settings!!.time > 0) {
                timerCheckBox.isChecked = true
                minute.setText((settings!!.time / 60).toString())
                if (settings!!.time % 60 > 0)
                    second.setText((settings!!.time % 60).toString())
            }
        }

        return layout
    }

    private fun dialogBox(message: String) {
        AlertDialog.Builder(activity!!)
                .setMessage(message)
                .setPositiveButton("ОК") {
                    _, _ ->
                    if (isChanging)
                        onTestUpdateListener.onTestUpdated(getData())
                    else
                        onTestSaveListener.onTestSaving(getData())
                }
                .setNegativeButton("Отмена") { _, _ -> }
                .create()
                .show()
    }

    private fun checkEmpty(): Int {
        if (layout.test_name__creating.length() == 0)
            return EMPTY_NAME
        if (layout.timer_checkbox.isChecked && getTime() == 0)
            return EMPTY_TIMER
        return 0
    }

    private fun getTime(): Int {
        var time = 0
        if (layout.timer_minute.length() > 0)
            time += layout.timer_minute.text.toString().toInt() * 60
        if (layout.timer_second.length() > 0)
            time += layout.timer_second.text.toString().toInt()
        return time
    }

    private fun getData() =
            SettingsData(
                    layout.test_name__creating.text.toString(),
                    layout.seekbar__five.progress,
                    layout.seekbar__four.progress,
                    layout.seekbar__three.progress,
                    if (layout.show_wrong.isChecked) "t" else "f",
                    if (layout.timer_checkbox.isChecked) getTime() else 0
            )

}