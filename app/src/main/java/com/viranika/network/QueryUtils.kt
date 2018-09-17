/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.viranika.network

import android.text.TextUtils
import android.util.Log

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.ArrayList

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
object QueryUtils {

    /** Tag for the log messages  */
    private val LOG_TAG = QueryUtils::class.java.simpleName

    /**
     * Query the USGS dataset and return a list of [Earthquake] objects.
     */
    fun fetchEarthquakeData(requestUrl: String): List<Earthquake>? {
        // Create URL object
        val url = createUrl(requestUrl)

        // Perform HTTP request to the URL and receive a JSON response back
        var jsonResponse: String? = null
        try {
            jsonResponse = makeHttpRequest(url)
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e)
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s

        // Return the list of {@link Earthquake}s
        return extractFeatureFromJson(jsonResponse)
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private fun createUrl(stringUrl: String): URL? {
        var url: URL? = null
        try {
            url = URL(stringUrl)
        } catch (e: MalformedURLException) {
            Log.e(LOG_TAG, "Problem building the URL ", e)
        }

        return url
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    @Throws(IOException::class)
    private fun makeHttpRequest(url: URL?): String {
        var jsonResponse = ""

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse
        }

        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.readTimeout = 10000
            urlConnection.connectTimeout = 15000
            urlConnection.requestMethod = "GET"
            urlConnection.connect()

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.responseCode == 200) {
                inputStream = urlConnection.inputStream
                jsonResponse = readFromStream(inputStream)
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.responseCode)
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e)
        } finally {
            urlConnection?.disconnect()
            inputStream?.close()
        }
        return jsonResponse
    }

    /**
     * Convert the [InputStream] into a String which contains the
     * whole JSON response from the server.
     */
    @Throws(IOException::class)
    private fun readFromStream(inputStream: InputStream?): String {
        val output = StringBuilder()
        if (inputStream != null) {
            val inputStreamReader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
            val reader = BufferedReader(inputStreamReader)
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line)
                line = reader.readLine()
            }
        }
        return output.toString()
    }

    /**
     * Return a list of [Earthquake] objects that has been built up from
     * parsing the given JSON response.
     */
    private fun extractFeatureFromJson(earthquakeJSON: String?): List<Earthquake>? {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(earthquakeJSON)) {
            return null
        }

        // Create an empty ArrayList that we can start adding earthquakes to
        val earthquakes = ArrayList<Earthquake>()

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            val baseJsonResponse = JSONObject(earthquakeJSON)

            // Extract the JSONArray associated with the key called "features",
            // which represents a list of features (or earthquakes).
            val earthquakeArray = baseJsonResponse.getJSONArray("features")

            // For each earthquake in the earthquakeArray, create an {@link Earthquake} object
            for (i in 0 until earthquakeArray.length()) {

                // Get a single earthquake at position i within the list of earthquakes
                val currentEarthquake = earthquakeArray.getJSONObject(i)

                // For a given earthquake, extract the JSONObject associated with the
                // key called "properties", which represents a list of all properties
                // for that earthquake.
                val properties = currentEarthquake.getJSONObject("properties")

                // Extract the value for the key called "mag"
                val magnitude = properties.getDouble("mag")

                // Extract the value for the key called "place"
                val location = properties.getString("place")

                // Extract the value for the key called "time"
                val time = properties.getLong("time")

                // Extract the value for the key called "url"
                val url = properties.getString("url")

                // Create a new {@link Earthquake} object with the magnitude, location, time,
                // and url from the JSON response.
                val earthquake = Earthquake(magnitude, location, time, url)

                // Add the new {@link Earthquake} to the list of earthquakes.
                earthquakes.add(earthquake)
            }

        } catch (e: JSONException) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e)
        }

        // Return the list of earthquakes
        return earthquakes
    }

}
/**
 * Create a private constructor because no one should ever create a [QueryUtils] object.
 * This class is only meant to hold static variables and methods, which can be accessed
 * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
 */
