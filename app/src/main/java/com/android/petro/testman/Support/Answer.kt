package com.android.petro.testman.Support

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by petro on 27.07.2017.
 * Class for storing, saving and getting answer's information
 */

class Answer(private val id: Int,
             val answer: ArrayList<Any>,
             val time: Int,
             val test: Int,
             var mark: Int = -1,
             var date: Int = -1,
             var author: Int = -1) {

    fun sendData(context : Context, callback: OnAnswerSavedListener, test: Test) {
        try {
            val dialog = ProgressDialog(context)
            var earnedScores = 0.0f
            var allScores = 0.0f
            val tasks = test.tasks.tasks
            for (i in 0 until tasks.size) {
                if (tasks[i].type == TaskType.RADIO_BOX.code) {
                    if ((tasks[i].rights as Double).toInt() == answer[i])
                        earnedScores += tasks[i].scores
                    allScores += tasks[i].scores
                } else if (tasks[i].type == TaskType.CHECK_BOX.code) {
                    allScores += tasks[i].scores
                    val rightList = tasks[i].rights as ArrayList<Double>
                    val list = answer[i] as ArrayList<Int>
                    var wrongs = 0
                    if (Math.abs(rightList.size - list.size) > 1)
                        break
                    if (rightList.size == list.size + 1)
                        wrongs = 1
                    Log.i("TestManInformation", "List: ${Arrays.asList(list)}, rightList: ${Arrays.asList(rightList)}, lolol: ${rightList[0]}")
                    for (j in 0 until rightList.size) {
                        if (!list.contains(rightList[j].toInt())) {
                            Log.d("TestManDebug", "Answer 1")
                            wrongs++
                            if (wrongs == 2)
                                break
                        }
                    }
                    earnedScores += when(wrongs) {
                        0 ->
                            tasks[i].scores
                        1 ->
                            tasks[i].scores / 2
                        else ->
                            0
                    }
                }
            }
            val percents = earnedScores * 100.0f / allScores
            mark = when {
                percents >= test.settings.fivebegins -> 5
                percents >= test.settings.fourbegins -> 4
                percents >= test.settings.threebegins -> 3
                else -> 2
            }
            object : AsyncTask<Void, Void, String>() {
                override fun onPreExecute() {
                    super.onPreExecute()
                    dialog.setTitle("TestMan")
                    dialog.setMessage("Подождите")
                    dialog.setCancelable(false)
                    dialog.show()
                }
                override fun doInBackground(vararg params: Void): String {
                    val body = FormBody.Builder()
                            .add("answer", answer.toString())
                            .add("time", time.toString())
                            .add("id", id.toString())
                            .add("mark", mark.toString())
                            .build()

                    Log.d("Answer on test", answer.toString())

                    val request = Request.Builder()
                            .url("https://testman-o442a4o3.c9users.io/add_answer/")
                            .post(body)
                            .build()

                    return OkHttpClient().newCall(request).execute().body().string()
                }
                override fun onPostExecute(result: String?) {
                    super.onPostExecute(result)
                    Log.v("HttpResponse", result)
                    dialog.hide()
                    callback.onAnswerSaved(this@Answer, mark)
                }
            }.execute()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun get(id: Int, context: Context, callback: OnAnswerReceivedListener) {
            val dialog = ProgressDialog(context)
            dialog.setMessage("Загружаем ответ")
            dialog.setCancelable(false)
            dialog.setTitle("TestMan")
            object: AsyncTask<Void, Void, Answer>() {
                override fun onPreExecute() {
                    super.onPreExecute()
                    dialog.show()
                }

                override fun doInBackground(vararg params: Void?): Answer {
                    val formBody = FormBody.Builder()
                            .add("id", id.toString())
                            .build()

                    val request = Request.Builder()
                            .url("https://testman-o442a4o3.c9users.io/get_answer_by_id/")
                            .post(formBody)
                            .build()

                    val responseString = OkHttpClient().newCall(request).execute().body().string()
                    Log.i("TestManNetwork", "ReceivedAnswer: $responseString")

                    val gson = Gson()
                    val pseudoAnswer = gson.fromJson(responseString.toString(), PseudoAnswer::class.java)
                    val array = ArrayList<Any>()
                    if (pseudoAnswer.answer != null) {
                        Log.i("TestManInformation", "JsonArray: ${pseudoAnswer.answer}")
                        val string = pseudoAnswer.answer
                        val jsonArray = JSONArray(string)
                        (0 until jsonArray.length()).mapTo(array) { jsonArray.get(it) }
                    }
                    return Answer(pseudoAnswer.id,
                            array,
                            pseudoAnswer.time,
                            pseudoAnswer.test,
                            pseudoAnswer.mark,
                            pseudoAnswer.date,
                            pseudoAnswer.author)
                }

                override fun onPostExecute(result: Answer) {
                    super.onPostExecute(result)
                    dialog.hide()
                    callback.onAnswerReceived(result)
                }
            }.execute()
        }

        private class PseudoAnswer(val id: Int,
                                   val answer: String?,
                                   val time: Int,
                                   val test: Int,
                                   val mark: Int,
                                   val date: Int,
                                   val author: Int)
    }
}
