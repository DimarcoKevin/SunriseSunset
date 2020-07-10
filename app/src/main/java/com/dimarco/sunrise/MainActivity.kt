package com.dimarco.sunrise

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

    // create method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // method is called during both API calls for location and sunInfo
    fun convertStreamToString(inputStream: InputStream): String {
        val bufferReader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        var allString = ""

        try {
            do {
                line = bufferReader.readLine()
                if (line != null) {
                    allString += line
                }
            } while (line != null)
            inputStream.close()
        } catch (ex: Exception) { }

        return allString
    }

    // clickable submit method (main method)
    fun getSunriseSunset(view: View) {
        var city = txtInput.text
        val url = "https://www.metaweather.com/api/location/search/?query=$city"
        LocationAsyncTask().execute(url)
    }

    // clickable clear button
    fun clickClear(view: View) {
        val input = R.string.input_hint
        val output = R.string.dashes
        txtInput.hint = input.toString()
        txtOutput.text = output.toString()
    }

    // async task to get the "where on earth ID"
    inner class LocationAsyncTask: AsyncTask<String, String, String>() {

        // sets up the connection to the URL destination
        override fun doInBackground(vararg p0: String?): String {
            var urlConnection: HttpURLConnection? = null

            try {
                val url = URL(p0[0])
                urlConnection = url.openConnection() as HttpURLConnection // TODO : may need to change to https
                urlConnection.connectTimeout=7000

                var inString = convertStreamToString(urlConnection.inputStream)
                publishProgress(inString)
            } catch (ex: Exception) {

            }  finally {
                urlConnection?.disconnect()
            }
            return ""
        }

        // search through the json file for "where on earth ID" then starts next Async Task
        override fun onProgressUpdate(vararg values: String) {

            try {
                var json = JSONArray(values[0])
                var jsonObject = json.getJSONObject(0)
                var woeid = jsonObject.getString("woeid")
                var woeidUrl = "https://www.metaweather.com/api/location/$woeid"
                SunAsyncTask().execute(woeidUrl)
            } catch (ex: Exception) { ex.printStackTrace() }

        }

    }

    // async task using the woeid to return the sunrise and sunset times
    inner class SunAsyncTask: AsyncTask<String, String, String>() {
        override fun onPreExecute() {
            // fires before task starts
            // NOT NEEDED FOR THIS PROJECT
        }

        // searches through the json file for sunrise and sunset and then gathers a substring for the time of day
        override fun onProgressUpdate(vararg values: String) {

            try {
                var json = JSONObject(values[0])
                var sunrise = json.getString("sun_rise")
                var sunset = json.getString("sun_set")
                sunrise = sunrise.substring(11, 19)
                sunset = sunset.substring(11, 19)

                txtOutput.text = "Sunrise: $sunrise \nSunset: $sunset"

            } catch (ex: Exception) {  }
        }

        override fun onPostExecute(result: String?) {
            // fires after task ends
            // NOT NEEDED FOR THIS PROJECT
        }

        // sets up the connection to the URL destination
        override fun doInBackground(vararg p0: String?): String {
            var urlConnection: HttpsURLConnection? = null

            try {
                val url = URL(p0[0])
                val urlConnection = url.openConnection() as HttpURLConnection // TODO : may need to change to https
                urlConnection.connectTimeout=7000

                var inString = convertStreamToString(urlConnection.inputStream)
                publishProgress(inString)
            } catch (ex: Exception) {

            } finally {
                urlConnection?.disconnect()
            }
            return ""
        }
    }
}
