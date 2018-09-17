package com.viranika.network

import android.app.LoaderManager
import android.content.Context
import android.content.Intent
import android.content.Loader
import android.net.ConnectivityManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<List<Earthquake>> {

    private val LOG_TAG = MainActivity::class.java!!.getName()

    /** URL for earthquake data from the USGS dataset  */
    private val USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&orderby=time&minmag=6&limit=10"

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    val EARTHQUAKE_LOADER_ID = 1

    /** Adapter for the list of earthquakes  */
    var mAdapter: EarthquakeAdapter? = null

    /** TextView that is displayed when the list is empty  */
    var mEmptyStateTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find a reference to the {@link ListView} in the layout
        val earthquakeListView = list

        mEmptyStateTextView = empty_view
        earthquakeListView.emptyView = mEmptyStateTextView

        // Create a new adapter that takes an empty list of earthquakes as input
        mAdapter = EarthquakeAdapter(this, ArrayList<Earthquake>())

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.adapter = mAdapter

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected earthquake.
        earthquakeListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, l ->
            // Find the current earthquake that was clicked on
            val currentEarthquake = mAdapter!!.getItem(position)

            // Convert the String URL into a URI object (to pass into the Intent constructor)
            val earthquakeUri = Uri.parse(currentEarthquake.url)

            // Create a new intent to view the earthquake URI
            val websiteIntent = Intent(Intent.ACTION_VIEW, earthquakeUri)

            // Send the intent to launch a new activity
            startActivity(websiteIntent)
        }

        // Get a reference to the ConnectivityManager to check state of network connectivity
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Get details on the currently active default data network
        val networkInfo = connMgr.activeNetworkInfo

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            val loaderManager = loaderManager

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader<List<Earthquake>>(EARTHQUAKE_LOADER_ID, null, this)
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            val loadingIndicator = loading_indicator
            loadingIndicator.setVisibility(View.GONE)

            // Update empty state with no connection error message
            mEmptyStateTextView!!.setText(R.string.no_internet_connection)
        }
    }

    override fun onCreateLoader(i: Int, bundle: Bundle): Loader<List<Earthquake>> {
        // Create a new loader for the given URL
        return EarthquakeLoader(this, USGS_REQUEST_URL)
    }

    override fun onLoadFinished(loader: Loader<List<Earthquake>>, earthquakes: List<Earthquake>?) {
        // Hide loading indicator because the data has been loaded
        val loadingIndicator = loading_indicator
        loadingIndicator.setVisibility(View.GONE)

        // Set empty state text to display "No earthquakes found."
        mEmptyStateTextView!!.setText(R.string.no_earthquakes)

        // Clear the adapter of previous earthquake data
        //mAdapter.clear();

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (earthquakes != null && !earthquakes.isEmpty()) {
            //updateUi(earthquakes)
            mAdapter!!.addAll(earthquakes)
        }
    }

    override fun onLoaderReset(loader: Loader<List<Earthquake>>) {
        // Loader reset, so we can clear out our existing data.
        mAdapter!!.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val settingsIntent = Intent(this, SettingActivity::class.java)
            startActivity(settingsIntent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
