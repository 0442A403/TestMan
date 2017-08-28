package com.android.petro.testman.Activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.petro.testman.Fragments.Create.CreateFragment
import com.android.petro.testman.R
import com.android.petro.testman.Support.Listeners.OnTestReceivedListener
import com.android.petro.testman.Support.Listeners.OnTestSavedListener
import com.android.petro.testman.Support.Listeners.OnTestUpdatedListener
import com.android.petro.testman.Support.TestData.Answer
import com.android.petro.testman.Support.TestData.Test
import kotlinx.android.synthetic.main.activity_changing.*

class ChangingActivity:
        AppCompatActivity(), OnTestSavedListener, OnTestReceivedListener, OnTestUpdatedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changing)
        setSupportActionBar(toolbar__changing)
        title = "Изменить тест"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        Test.get(this, this, intent.getIntExtra("Test id", -1))
    }

    override fun onTestSaved() {

    }

    override fun onTestReceived(test: Test) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout__changing, CreateFragment(supportFragmentManager, this, test, intent.getIntExtra("Test id", -1)))
                .commit()
    }

    override fun onTestReceived(test: Test, answer: Answer?) {
        finish()
    }

    override fun onTestReceived(test: Test, receivedId: Int) {
        finish()
    }

    override fun onTestUpdated() {
        setResult(BaseActivity.RESULT_TEST_UPDATED)
        finish()
    }
}
