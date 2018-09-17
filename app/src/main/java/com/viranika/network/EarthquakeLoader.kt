package com.viranika.network

import android.content.AsyncTaskLoader
import android.content.Context

class EarthquakeLoader
/**
 * Constructs a new [EarthquakeLoader].
 *
 * @param context of the activity
 * @param url to load data from
 */
(context: Context,
 /** Query URL  */
 private val mUrl: String?) : AsyncTaskLoader<List<Earthquake>>(context) {

    override fun onStartLoading() {
        forceLoad()
    }

    /**
     * This is on a background thread.
     */
    override fun loadInBackground(): List<Earthquake>? {
        return if (mUrl == null) {
            null
        } else QueryUtils.fetchEarthquakeData(mUrl)

        // Perform the network request, parse the response, and extract a list of earthquakes.
    }

    companion object {

        /** Tag for log messages  */
        private val LOG_TAG = EarthquakeLoader::class.java.name
    }
}