package com.android.petro.testman.Fragments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.android.petro.testman.R
import com.android.petro.testman.Support.CreateCallBack
import com.android.petro.testman.Support.SettingsClass
import com.pawegio.kandroid.onProgressChanged
import com.pawegio.kandroid.textWatcher
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlin.properties.Delegates

/**
 * Fragment for test's settings
 */

class SettingsFragment(val callback: CreateCallBack) : Fragment() {

    private var layout: FrameLayout by Delegates.notNull()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        layout = inflater!!.inflate(R.layout.fragment_settings, container, false) as FrameLayout

        val five = layout.seekbar__five
        val four = layout.seekbar__four
        val three = layout.seekbar__three
        val fiveIndicator = layout.seekbar_indicator__five
        val fourIndicator = layout.seekbar_indicator__four
        val threeIndicator = layout.seekbar_indicator__three

        five.onProgressChanged {
            progress, _ ->
            if (progress < 3)
                five.progress = 3
            if (progress <= four.progress)
                four.progress = progress - 1
            fiveIndicator.text = "${five.progress}%"
        }
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
            val i : Int = four.progress
            fourIndicator.text = "${four.progress}%"
        }
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

        val showWrong = layout.show_wrong
        val timerCheckBox = layout.timer_checkbox
        val timerWrapper: View = layout.timer_wrapper

        timerCheckBox.setOnCheckedChangeListener {
            _, isChecked : Boolean ->
            if (isChecked)
                timerWrapper.visibility = View.VISIBLE
            else
                timerWrapper.visibility = View.GONE
        }

        val minute = layout.timer_minute
        val second = layout.timer_second

        minute.setOnEditorActionListener { _, _, _ -> minute.requestFocus() }
        second.textWatcher {
            onTextChanged { _, _, _, _ ->
                if (minute.length() == 2 && minute.text.toString().toInt() > 59)
                    minute.setText(minute.text.substring(0..0))
            }
        }

        layout.save_test.setOnClickListener { v ->
            if (checkEmpty())
                Snackbar.make(v, "Введите время", Snackbar.LENGTH_SHORT).show()
            else {
                dialogBox(getTime() < 300)
            }
        }

        return layout
    }

    fun dialogBox(b: Boolean) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setMessage(if (b) "Вы выбрали время, меньшее 5 минут, сохранить?" else "Сохранить")
        alertDialogBuilder.setPositiveButton("ОК") {
            arg0, arg1 -> callback.onTestSave(saveData())
        }
        alertDialogBuilder.setNegativeButton("Отмена") {
            arg0, arg1 ->
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun checkEmpty(): Boolean {
        if (layout.test_name__creating.length() == 0
                || (layout.timer_checkbox.isChecked && getTime() == 0))
            return true
        return false
    }

    fun getTime() = layout.timer_minute.text.toString().toInt() * 60 + layout.timer_second.text.toString().toInt()

    fun saveData() =
        SettingsClass(
                layout.test_name__creating.text.toString(),
                layout.seekbar__five.progress,
                layout.seekbar__four.progress,
                layout.seekbar__three.progress,
                layout.show_wrong.isChecked,
                getTime()
        )

}
